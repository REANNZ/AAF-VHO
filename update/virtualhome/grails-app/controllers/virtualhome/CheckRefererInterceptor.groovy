package virtualhome

class CheckRefererInterceptor {

    String VALID_REFERER

    CheckRefererInterceptor() {
        VALID_REFERER = "^${grailsApplication.config.grails.serverURL}".replace("http", "https?")
        matchAll()
    }

    boolean before() {
        if (request.method.toUpperCase() != "GET" && request.method.toUpperCase() != "HEAD" && !request.forwardURI.startsWith("/api")) {
          def referer = request.getHeader('Referer')

          if(!(referer && referer =~ VALID_REFERER)) {
            log.error("DENIED - ${request.remoteAddr}|$params.controller/$params.action - Referer: $referer was not valid, should have been a match for $VALID_REFERER")
            response.sendError(403)
            return false
          }
        }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
