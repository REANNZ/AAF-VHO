package aaf.base.workflow

import org.codehaus.groovy.control.CompilationFailedException

import aaf.base.identity.Subject

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class WorkflowScript {
  static auditable = true
  
  def grailsApplication
  
  String name
  String description
  String definition
  
  Subject creator
  Subject lastEditor
  
  Date dateCreated
  Date lastUpdated
  
  static mapping = {
    definition type: "text"
  }
  
  static constraints = {
    name(nullable: false, blank:false, unique: true)
    description(nullable: true, blank:false)
    definition(nullable: false, blank: false, validator: { val, obj ->
      obj.validateScript()
    })
    
    creator(nullable: false)
    lastEditor(nullable: true)
  }
  
  def validateScript() {
    try {
      new GroovyShell(grailsApplication.classLoader).parse(definition)
    }
    catch(CompilationFailedException e) {
      log.error "Compilation error when compiling workflowscript:[id:$id, name:$name, description:$description]"
      log.debug e
      return ['workflow.task.validation.workflowscript.parse.invalid', e.getLocalizedMessage()]
    }
    true
  }
}
