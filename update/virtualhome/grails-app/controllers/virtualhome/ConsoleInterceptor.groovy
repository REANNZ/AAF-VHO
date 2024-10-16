package virtualhome

class ConsoleInterceptor {

    boolean before() {
        if (!grailsApplication.config.aaf.base.bootstrap) {
            if(accessControl { permission("app:administration") }) {
            log.info("secfilter: ALLOWED CONSOLE - [${subject?.id}]${subject?.principal}|${request.remoteAddr}|$params.controller/$params.action")
            return true
            }

            log.info("secfilter: DENIED CONSOLE - [${subject?.id}]${subject?.principal}|${request.remoteAddr}|$params.controller/$params.action")
            response.sendError(404) // Deliberately not 403 so endpoint can't be figured out.
            return false
        }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
