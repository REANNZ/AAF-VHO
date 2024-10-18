package virtualhome

class CheckRefererInterceptor {

    CheckRefererInterceptor() {
        matchAll()
    }

    boolean before() {
        if (request.method.toUpperCase() != "GET" && request.method.toUpperCase() != "HEAD" && !request.forwardURI.startsWith("/api")) {
          def referer = request.getHeader('Referer')
          def valid_referer = "^${grailsApplication.config.grails.serverURL}".replace("http", "https?")

          if(!(referer && referer =~ valid_referer)) {
            log.error("DENIED - ${request.remoteAddr}|$params.controller/$params.action - Referer: $referer was not valid, should have been a match for $valid_referer")
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
