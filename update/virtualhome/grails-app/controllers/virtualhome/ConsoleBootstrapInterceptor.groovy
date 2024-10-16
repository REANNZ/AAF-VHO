package virtualhome

class ConsoleBootstrapInterceptor {

    ConsoleBootstrapInterceptor() {
        match controller:"console"
    }

    boolean before() { 
        if (grailsApplication.config.aaf.base.bootstrap) {
          log.info("secfilter: ALLOWED BOOTSTRAP CONSOLE - ${request.remoteAddr}|$params.controller/$params.action")
          return true
        }

        accessControl { true }
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
