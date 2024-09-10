package aaf.base.identity

class DevelopmentAttributesService {
  public static final String KEY_NAME = "aaf.base.identity.developmentAttributes"

  def grailsApplication

  public void injectAttributes(request, session) {
    if (grailsApplication.config.aaf.base.realms.federated.development.active) {
      if (grailsApplication.config.aaf.base.realms.federated.request.attributes) {
        def attributes = session[KEY_NAME]
        def req = request

        attributes.each { attr, value ->
          req.setAttribute(attr, value)
        }
      } else {
        throw new UnsupportedOperationException("Injecting attributes as request headers has not been implemented")
      }
    }
  }

  public void storeAttributes(request, session, params) {
    if (request.post && grailsApplication.config.aaf.base.realms.federated.development.active) {
      def attributes = grailsApplication.config.aaf.base.realms.federated.mapping.values()

      // Add the attributes into the session for recall later, so we can fake a real shib setup if we need
      // other attributes in the app. e.g. (and first use case) the SnapshotController in attributevalidator
      session[KEY_NAME] = attributes.inject([:]) { map, attr ->
        map[attr] = params["attributes.${attr}"]
        map
      }
    }
  }
}
