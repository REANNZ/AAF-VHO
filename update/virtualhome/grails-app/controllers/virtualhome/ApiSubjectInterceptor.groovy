package virtualhome

class ApiSubjectInterceptor {

    ApiSubjectInterceptor() {
        match(controller: 'apiSubject').except(action: ['index', 'list', 'create', 'save'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "Subject ID was not present"

            flash.type = 'info'
            flash.message = 'controllers.aaf.base.identity.apisubject.nosubjectid'

            redirect action:'list'
            return false
        }

        def apiSubject = ApiSubject.get(params.id)
        if (!apiSubject) {
            log.warn "No apiSubject for $params.id located"

            flash.type = 'error'
            flash.message = 'controllers.aaf.base.identity.apisubject.nonexistant'

            redirect(action: "list")
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
