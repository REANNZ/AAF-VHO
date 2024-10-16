package virtualhome

class DevelopmentAttributesInterceptor {

    def developmentAttributesService

    DevelopmentAttributesInterceptor() {
        match controller:'federatedSessions', action:'federatedlogin'
    }

    boolean before() {
        if (grailsApplication.config.aaf.base.realms.federated.development.active) {
          developmentAttributesService.storeAttributes(request, session, params)
          developmentAttributesService.injectAttributes(request, session)
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
