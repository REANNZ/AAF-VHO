package virtualhome

class SubjectInterceptor {

    SubjectInterceptor() {
        match(controller:'subject').except(action: ['index', 'list'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "Subject ID was not present"

            flash.type = 'info'
            flash.message = 'controllers.aaf.base.identity.subject.nosubjectid'

            redirect action:'list'
            return false
        }

        def subject = Subject.get(params.id)
        if (!subject) {
            log.warn "No subject for $params.id located"

            flash.type = 'error'
            flash.message = 'controllers.aaf.base.identity.subject.nonexistant'

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
