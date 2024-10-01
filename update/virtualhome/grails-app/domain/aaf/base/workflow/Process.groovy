package aaf.base.workflow

import aaf.base.identity.Subject

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.plugins.orm.auditable.Auditable

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class Process implements Auditable {
  String name
  String description
  String definition
  
  boolean active
  
  int version
  int processVersion
  
  Subject creator
  
  Date dateCreated
  Date lastUpdated
  
  List tasks
  
  static hasMany = [tasks: Task, instances: ProcessInstance]
  
  static mapping = {
    definition type: "text"
  }
  
  static constraints = {
    name(nullable: false, blank:false)
    description(nullable: false, blank:false)
    processVersion(min: 1)
    
    dateCreated(nullable:true)
    lastUpdated(nullable:true)

    definition(nullable: false)
    creator(nullable: false)
    
    tasks(validator: {val, obj ->
      obj.validateTasks()
    })
  }
  
  def validateTasks = {
    boolean f = false
    
    // Ensure all processes have at least 1 task
    if(tasks == null || tasks.size() == 0) {
      return ['workflow.process.validation.tasks.minimum', name]
    }
    
    for(v in tasks) {
      // Ensure all dependencies reference valid tasks
      for(dep in v.dependencies) {
        def task = tasks.find { t -> t.name == dep }
        if(!task) {
          return ['workflow.process.validation.tasks.dependencies.invalid.reference', name, dep]
        }
      }
      
      // Ensure all outcome start+terminate reference valid tasks
      for(out in v.outcomes.values()) {
        for (s in out.start) {
          def task = tasks.find { t -> t.name == s }
          if(!task) {
            return ['workflow.process.validation.tasks.outcomes.invalid.start.reference', name, s]
          }
        }
        for (s in out.terminate) {
          def task = tasks.find { t -> t.name == s }
          if(!task) {
            return ['workflow.process.validation.tasks.outcomes.invalid.terminate.reference', name, s]
          }
        }
      }
      
      // Ensure all rejections start+terminate reference valid tasks
      for(rej in v.rejections.values()) {
        for (s in rej.start) {
          def task = tasks.find { t -> t.name == s }
          if(!task) {
            return ['workflow.process.validation.tasks.rejections.invalid.start.reference', name, s]
          }
        }
        for (s in rej.terminate) {
          def task = tasks.find { t -> t.name == s }
          if(!task) {
            return ['workflow.process.validation.tasks.rejections.invalid.terminate.reference', name, s]
          }
        }
      }
      
      if(!f)
        f = v.finishOnThisTask
    }
    
    // Ensure all processes have at least 1 finish task
    if(!f)
      return ['workflow.process.validation.no.finish.task', name]
    
    true
  }
}
