package aaf.base.workflow

import aaf.base.identity.Subject

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.plugins.orm.auditable.Auditable

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class TaskInstance implements Auditable {
  TaskStatus status = TaskStatus.PENDING
  Subject approver

  Date dateCreated
  Date lastUpdated

  static hasMany = [messages: WorkflowMessage, potentialApprovers: Subject]
  static belongsTo = [processInstance: ProcessInstance, task: Task]
  static constraints = {
    approver(nullable:true)
    dateCreated(nullable:true)
    lastUpdated(nullable:true)
  }
  
  static mapping = {
    cache usage:'read-write', include:'non-lazy'
  }
}
