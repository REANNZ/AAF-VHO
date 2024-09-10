package aaf.base.workflow

import aaf.base.identity.Subject

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class ProcessInstance {
  static auditable = true
  
  String description
  ProcessStatus status
  ProcessPriority priority

  String agentCN
  String agentEmail
  Subject agent   // Workflow created on behalf of known subject
  
  Date dateCreated
  Date lastUpdated
  
  boolean completionAcknowlegded = false
  
  List taskInstances
  Map params

  static hasMany = [taskInstances: TaskInstance, messages: WorkflowMessage]
  static belongsTo = [process: Process]

  static constraints = {
    agent(nullable:true)

    dateCreated(nullable: true)
    lastUpdated(nullable: true)
  }

  public void setAgent(Subject agent) {
    if(agent) {
      this.agent = agent
      this.agentCN = agent.cn
      this.agentEmail = agent.email
    }
  }
}
