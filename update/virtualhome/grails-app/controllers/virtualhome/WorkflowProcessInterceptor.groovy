package virtualhome

class WorkflowProcessInterceptor {

    WorkflowProcessInterceptor() {
        match(controller:'workflowProcess').except(action: ['list', 'create', 'save'])
    }

    boolean before() {
        if(!params.id) {
            log.warn "Process ID was not present"

            flash.type = 'info'
            flash.message = 'controllers.aaf.base.workflow.workflowprocess.noprocessid'

            redirect action:'list'
            return false
        }

        def process = Process.get(params.id)
        if(!process) {

            log.warn "Process identified by ${params.id} does not exist"

            flash.type = 'error'
            flash.message = 'controllers.aaf.base.workflow.workflowprocess.nonexistant'

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
