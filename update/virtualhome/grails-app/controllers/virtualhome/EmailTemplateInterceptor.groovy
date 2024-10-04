package virtualhome


class EmailTemplateInterceptor {

    EmailTemplateInterceptor() {
        match(controller:'emailTemplate').except(action: ['list', 'create', 'save'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "EmailTemplate ID was not present"

            flash.type = 'info'
            flash.message = message(code: 'controllers.aaf.base.admin.emailtemplate.noemailtemplateid')

            redirect action:'list'
            return false
        }

        def emailtemplate = EmailTemplate.get(params.id)
        if(!emailtemplate) {

            log.warn "EmailTemplate identified by ${params.id} does not exist"

            flash.type = 'error'
            flash.message = 'controllers.aaf.base.admin.emailtemplate.nonexistant'

            redirect action:'list'
            return false
        }

        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
