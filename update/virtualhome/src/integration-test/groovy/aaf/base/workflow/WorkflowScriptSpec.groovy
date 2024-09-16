package aaf.base.workflow

import grails.test.spock.*
import grails.test.mixin.TestMixin

import aaf.base.identity.Subject
import aaf.base.identity.Role

class WorkflowScriptSpec extends IntegrationSpec {
  static transactional = true
  
  def grailsApplication
  def subject
  
  def setup() {
    def role = new Role(name:'allsubjects')
    subject = new Subject(principal:'testuser', cn:'test user', email:'test@testdomain.com', sharedToken:'1234sharedToken').save()
    role.addToSubjects(subject)
    role.save()
  }
  
  def "validate a correctly formed WorkflowScript is saved"() {   
    when:
    def wfs = new WorkflowScript(name:'TestWorkflow', description:'Test workflow description', definition:'import domains.fr.workflow.*; return true', creator:Subject.findWhere(principal:'testuser')).save()
    
    then:
    !wfs.hasErrors()
    wfs == WorkflowScript.findByName('TestWorkflow')
  }
  
  def "validate a WorkflowScript with no creator isn't valid"() {
    expect:
    !subject.hasErrors()

    when:
    def wfs = new WorkflowScript(name:'TestWorkflow', description:'Test workflow description', definition:'import domains.fr.workflow.*; return true')
    wfs.save()
    
    then:
    wfs.hasErrors()
  }
  
  def "validate a non named WorkflowScript isn't valid"() {   
    expect:
    !subject.hasErrors()

    when:
    def wfs = new WorkflowScript(description:'Test workflow description', definition:'return true', creator:Subject.findWhere(principal:'testuser'))
    wfs.validate()
    
    then:
    wfs.hasErrors()
  }
  
  def "validate a non described WorkflowScript isn't valid"() {
    expect:
    !subject.hasErrors()
    
    when:
    def wfs = new WorkflowScript(name:'TestWorkflow', definition:'return true')
    wfs.validate()
    
    then:
    wfs.hasErrors()
  }
  
  def "validate a WorkflowScript with null execution definition is invalid"() {   
    expect:
    !subject.hasErrors()

    when:
    def wfs = new WorkflowScript(name:'TestWorkflow', description:'Test workflow description', definition:null, creator:Subject.findWhere(principal:'testuser'))
    wfs.validate()
    
    then:
    wfs.hasErrors()
    wfs.errors.getFieldError('definition').code == 'nullable'
  }
  
  def "validate a WorkflowScript with a blank execution definition is invalid"() {   
    expect:
    !subject.hasErrors()

    when:
    def wfs = new WorkflowScript(name:'TestWorkflow', description:'Test workflow description', definition:'', creator:Subject.findWhere(principal:'testuser'))
    wfs.validate()
    
    then:
    wfs.hasErrors()
    wfs.errors.getFieldError('definition').code == 'nullable'
  }
  
  def "validate a WorkflowScript with a non-parsable execution definition is invalid"() {  
    expect:
    !subject.hasErrors()

    when:
    def wfs = new WorkflowScript(name:'TestWorkflow', description:'Test workflow description', definition:'import this.will.never.exist; println x', creator:Subject.findWhere(principal:'testuser'))
    wfs.validate()
    
    then:
    wfs.hasErrors()
    wfs.errors.getFieldError('definition').code == 'workflow.task.validation.workflowscript.parse.invalid'
  }
  
  def "validate a correctly formed WorkflowScript is able to be executed"() {  
    setup:
    def wfs = new WorkflowScript(name:'TestWorkflow', description:'Test workflow description', definition:'import domains.fr.workflow.*; return "the test script validating execution ran fine"', creator:Subject.findWhere(principal:'testuser')).save()
    Binding binding = new Binding();
    binding.setVariable("grailsApplication", grailsApplication);
    
    def script = new GroovyShell().parse(wfs.definition)
    script.binding = binding

    expect:
    !subject.hasErrors()
    
    when:
    def outcome = script.run()
    
    then:
    !wfs.hasErrors()
    wfs == WorkflowScript.findByName('TestWorkflow')
    outcome == "the test script validating execution ran fine"
  }
}
