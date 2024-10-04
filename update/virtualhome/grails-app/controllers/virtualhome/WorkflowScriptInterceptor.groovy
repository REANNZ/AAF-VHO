package virtualhome

class WorkflowScriptInterceptor {

    WorkflowScriptInterceptor() {
        match(controller:'workflowScript').except(action: ['list', 'create', 'save'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "Script ID was not present"

            flash.type = 'info'
            flash.message = message(code: 'controllers.aaf.base.workflow.workflowscript.noscriptid')

            redirect action:'list'
            return false
        }

        def script = WorkflowScript.get(params.id)
        if(!script) {

            log.warn "Script identified by ${params.id} does not exist"

            flash.type = 'error'
            flash.message = 'controllers.aaf.base.workflow.workflowscript.nonexistant'

            redirect action:'list'
            return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
