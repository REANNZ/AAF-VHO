package virtualhome

import aaf.base.admin.EmailTemplate

class EmailTemplateInterceptor {

    EmailTemplateInterceptor() {
        match(controller:'emailTemplate', action: ['show', 'edit', 'update'])
    }

    boolean before() {
        if(!params.id) {
            redirect controller:'emailTemplate', action:'emailTemplateNoID'
            return true
        }

        def emailtemplate = EmailTemplate.get(params.id)
        if(!emailtemplate) {
            redirect controller:'emailTemplate', action:'emailTemplateNoTemplate'
            return true
        }
        return true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
