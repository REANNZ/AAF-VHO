package virtualhome

class AdministrationInterceptor {

    AdministrationInterceptor() {
        match uri: "/administration/**"
    }

    boolean before() { 
        if(!accessControl { permission("app:administration") }) {
          log.info("secfilter: DENIED - [${subject.id}]${subject.principal}|${request.remoteAddr}|$params.controller/$params.action")
          response.sendError(403)
          return false
        }
        log.info("secfilter: ALLOWED - [$subject.id]$subject.principal|${request.remoteAddr}|$params.controller/$params.action")
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
