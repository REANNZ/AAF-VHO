import aaf.vhr.*

import aaf.base.identity.*
import aaf.base.admin.*
import aaf.base.workflow.*

import org.apache.shiro.subject.Subject
import org.apache.shiro.util.ThreadContext
import org.apache.shiro.SecurityUtils

import grails.util.Environment

class AAFBaseBootStrap {

  def grailsApplication
  def roleService
  def permissionService

  def init = { servletContext ->

    if(Environment.current != Environment.TEST) {

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

    }

  }
}
