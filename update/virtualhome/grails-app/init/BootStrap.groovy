package virtualhome

import aaf.vhr.*

import aaf.base.identity.*
import aaf.base.admin.*
import aaf.base.workflow.*

import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import org.apache.shiro.SecurityUtils

import grails.util.Environment

class BoostStrap {

  def grailsApplication
  def roleService
  def permissionService
  def workflowProcessService

  // Inject the authenticated Subject object
  private void injectAuthn(def clazz) {
    clazz.metaClass.getPrincipal = {
      def subject = SecurityUtils.getSubject()
    }

    clazz.metaClass.getSubject = {
      def subject = null
      def principal = SecurityUtils.subject?.principal

      if(principal) {
        subject = aaf.base.identity.Subject.get(principal)
        log.debug "returning $subject"
      }
      subject
    }
  }

  def init = { servletContext ->

    if(Environment.current != Environment.TEST) {

        // This portion was taken from the file AAFBaseBootStrap.groovy

        // Useful for initial workflow population and other actions requiring an internal account
        def subject = aaf.base.identity.Subject.findWhere(principal:"aaf.base.identity:internal_account")
        if(!subject) {
        subject = new aaf.base.identity.Subject(principal:"aaf.base.identity:internal_account", enabled:false)
        if(!subject.save()) {
            subject.errors.each {
            println it
            }
            throw new RuntimeException("Unable to populate initial subject")
        }
        }

        // Populates the super user group if not present
        def adminRole = Role.findWhere(name:'AAF Application Administrators')
        if(!adminRole) {
        adminRole = roleService.createRole('AAF Application Administrators', 'AAF employees who have access to all parts of the application', true)

        def permission = new Permission()
        permission.type = Permission.defaultPerm
        permission.target = "*"
        
        permissionService.createPermission(permission, adminRole)

        log.warn("Created ${adminRole} for application wide administative access") 
        }

        def registered_managed_subject = EmailTemplate.findWhere(name:'role_invitation') 
        if(!registered_managed_subject) {
        def templateMarkup = grailsApplication.parentContext.getResource("classpath:aaf/base/identity/role_invitation.gsp").inputStream.text
        registered_managed_subject = new EmailTemplate(name:'role_invitation', content: templateMarkup)
        if(!registered_managed_subject.save()) {
            registered_managed_subject.errors.each {
            println it
            }
            throw new RuntimeException("Unable to populate initial role invitation email template role_invitation")
        }
        }

        // This portion was taken from the BootStrap.groovy file from the original virtualhome application

        def subject_vho = aaf.base.identity.Subject.findWhere(principal:"aaf.base.identity:internal_account")
        if(!subject) {
          throw new RuntimeException("Unable to retrieve initial subject reference \
            'aaf.base.identity:internal_account' which should be populated by base")
        }

        def organization_approval = WorkflowScript.findWhere(name:'organization_approval')
        if(!organization_approval) {
          def scriptMarkup = grailsApplication.parentContext.getResource("classpath:aaf/vhr/organization_approval.scr").inputStream.text
          organization_approval = new WorkflowScript(name:'organization_approval', description:'Executed to finalize new Organization', definition:scriptMarkup, , processVersion:1, creator:subject)
          if(!organization_approval.save()) {
            organization_approval.errors.each {
              println it
            }
            throw new RuntimeException("Unable to populate initial workflow script organization_approval")
          }
        }

        def organization_creation_process = Process.findWhere(name:'organization_creation_process')
        if(!organization_creation_process) {

          def suMetaClass = new ExpandoMetaClass(SecurityUtils)
          suMetaClass.'static'.getSubject = {[getPrincipal:{subject.id}] as Subject}
          suMetaClass.initialize()
          SecurityUtils.metaClass = suMetaClass

          def processMarkup = grailsApplication.parentContext.getResource("classpath:aaf/vhr/organization_creation_workflow.pr").inputStream.text
          workflowProcessService.create(processMarkup)

          SecurityUtils.metaClass = null
        }

        def seedEmailTemplate = { name ->
          def template = EmailTemplate.findWhere(name: name)
          if(!template) {
            def templateMarkup = grailsApplication.parentContext.getResource("classpath:aaf/vhr/${name}.gsp").inputStream.text
            template = new EmailTemplate(name: name, content: templateMarkup)
            if(!template.save()) {
              template.errors.each {
                println it
              }
              throw new RuntimeException("Unable to populate initial email template $name")
            }
          }
        }

        seedEmailTemplate('registered_managed_subject')
        seedEmailTemplate('deactivated_managed_subject')
        seedEmailTemplate('approved_new_organization')
        seedEmailTemplate('email_password_code')
        seedEmailTemplate('email_lost_username')
    }

    // Supply authenticated subject to filters
    grailsApplication.filtersClasses.each { filter ->
      // Should be used after verified call to 'accessControl'
      injectAuthn(filter.clazz)
    }

    // Supply authenticated subject to controllers
    grailsApplication.controllerClasses?.each { controller ->
      injectAuthn(controller.clazz)
    }

    // Supply authenticated subject to services
    grailsApplication.serviceClasses?.each { service ->
      injectAuthn(service.clazz)
    }
  }
}
