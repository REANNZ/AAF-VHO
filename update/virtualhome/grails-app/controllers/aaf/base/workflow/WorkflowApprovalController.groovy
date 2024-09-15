package aaf.base.workflow

import org.apache.shiro.SecurityUtils

class WorkflowApprovalController {
  static defaultAction = "list"
  
  def workflowTaskService

  def beforeInterceptor = [action: this.&validTaskInstance, except: ['list', 'administrative']]
  
  def list = {
    def tasks = workflowTaskService.retrieveTasksAwaitingApproval(subject)
    [tasks:tasks]
  }
  
  def administrative = {
    if(SecurityUtils.subject.isPermitted("app:administration")) {
      def c = TaskInstance.createCriteria()
      def tasks = c.listDistinct {
        and {
          eq("status", TaskStatus.APPROVALREQUIRED)
          processInstance {
            eq("status", ProcessStatus.INPROGRESS)
          }
        }
      }
      [tasks:tasks]
    }
    else {
      log.warn("Attempt to do administrative taskInstance listing by $subject was denied, not administrative user")
      response.sendError(403)
    }
  }
  
  def approve = {    
    def taskInstance = TaskInstance.get(params.id)
    
    if(taskInstance.potentialApprovers.contains(subject) || SecurityUtils.subject.isPermitted("app:administrator")) {
      log.info "$subject is approving $taskInstance"
      workflowTaskService.approve(taskInstance.id)

      flash.type = 'success'
      flash.message = 'controllers.aaf.base.workflow.workflowapproval.approve.success'
      
      log.info "$subject approval of $taskInstance completed"
      redirect action: "list"
    }
    else {
      log.warn("Attempt to approve $taskInstance by $subject was denied, no permission to modify this record")
      response.sendError(403)
    }
  }
  
  def reject = {
    if(!params.rejection) {
      log.warn "Rejection selection was not present"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.workflow.workflowapproval.reject.norejection'

      redirect action: "list"
      return
    }
    
    def taskInstance = TaskInstance.get(params.id)
    
    if(!taskInstance.task.rejections.containsKey(params.rejection)) {
      flash.type = "error"
      flash.message = message(code: 'domains.fr.workflow.taskinstance.no.such.rejection', args: [params.id])
      redirect action: "list"
      return
    }
    
    if(taskInstance.potentialApprovers.contains(subject) || SecurityUtils.subject.isPermitted("app:administrator")) {
      log.info "$subject is rejecting $taskInstance"
      workflowTaskService.reject(taskInstance.id, params.rejection)
      
      flash.type = 'success'
      flash.message = 'controllers.aaf.base.workflow.workflowapproval.reject.success'
      
      log.info "$subject rejection of $taskInstance completed"
      redirect action: "list"
    }
    else {
      log.warn("Attempt to reject $taskInstance with ${params.rejection} by $subject was denied, no permission to modify this record")
      response.sendError(403)
    }
  }

  private boolean validTaskInstance() {
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
  }

}
