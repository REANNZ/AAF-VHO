package virtualhome

class WorkflowApprovalInterceptor {

    WorkflowApprovalInterceptor() {
        match(controller:'workflowApproval').except(action: ['list', 'administrative'])
    }

    boolean before() {
        if(!params.id) {
        log.warn "TaskInstance ID was not present"
        
        flash.type = 'error'
        flash.message = 'controllers.aaf.base.workflow.workflowapproval.notaskinstanceid'
        
        redirect action: "list"
        return false
        }

        def taskInstance = TaskInstance.get(params.id)
        if(!taskInstance) {
        log.warn "TaskInstance identified by ${params.id} does not exist"

        flash.type = 'error'
        flash.message = 'controllers.aaf.base.workflow.workflowapproval.nonexistant'

        redirect action: "list"
        return false
        }

        true
    }

    boolean after() { true }

    void afterView() {
        // no-op
    }
}
