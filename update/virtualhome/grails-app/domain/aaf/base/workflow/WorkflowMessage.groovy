
package aaf.base.workflow

import aaf.base.identity.Subject

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class WorkflowMessage {
  static auditable = true

  Subject creator
  String message
  Date dateCreated

  static belongsTo = [processInstance: ProcessInstance, taskInstance: TaskInstance]

  static constraints = {
    message(nullable:false, blank:false)
    creator(nullable:true)
    
    processInstance(validator: {val, obj ->
      if(val == null && obj.taskInstance == null) {
        return ['workflow.workflowmessage.validation.processinstance.and.taskinstance.not.null']
      }
    })
    
    taskInstance(validator: {val, obj ->
      if(val == null && obj.taskInstance == null) {
        return ['workflow.workflowmessage.validation.taskinstance.and.processinstance.not.null']
      }
    })
  }
}
