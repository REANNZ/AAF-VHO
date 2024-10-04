package aaf.vhr

import groovy.time.TimeCategory
import org.springframework.context.i18n.LocaleContextHolder

import aaf.base.identity.Role
import aaf.base.admin.EmailTemplate
import aaf.vhr.switchch.vho.DeprecatedSubject

class LostPasswordController {

  static allowedMethods = [obtainsubject: 'POST', validatereset: 'POST']

  final String CURRENT_USER = "aaf.vhr.LostPasswordController.CURRENT_USER"
  final String EMAIL_CODE_SUBJECT ='controllers.aaf.vhr.lostpassword.email.code.subject'

  final static defaultAction = "start"

  def messageSource
  def passwordValidationService
  def cryptoService
  def emailManagerService
  def smsDeliveryService

  def start() {
  }

  def obtainsubject() {
    def managedSubjectInstance
    if (params.login) {
        managedSubjectInstance = ManagedSubject.findWhere(login: params.login)
    }

    if(!managedSubjectInstance) {
      log.error "No ManagedSubject representing ${params.login} found, requesting login before accessing password change"

      flash.type = 'info'
      flash.message = 'controllers.aaf.vhr.lostpassword.requiresaccount'
      redirect action: 'start'


    } else {
      session.setAttribute(CURRENT_USER, managedSubjectInstance.id)

      if(!managedSubjectInstance.canChangePassword()) {
        log.error "Unable to reset password for $managedSubjectInstance as account is not currently able to change passwords"
        redirect action: 'support'

        return
      }

      if(!managedSubjectInstance.isFinalized()) {
        log.error "Unable to reset password for $managedSubjectInstance as account has not been finalized"
        redirect action: 'support'

        return
      }

      redirect action: 'reset'
    }
  }

  def reset() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    // When not using 2FA, generate and send a resetCode if we don't have one yet.
    // When using 2FA and the user has a mobileNumber, generate and send a resetCodeExternal if we don't have one yet.
    // When using 2FA and the user does not have a mobileNumber, do nothing (skip this block) - the user needs to get the resetCodeExternal code out-of-band
    if( (!grailsApplication.config.aaf.vhr.passwordreset.second_factor_required && managedSubjectInstance.resetCode == null) ||
         (grailsApplication.config.aaf.vhr.passwordreset.second_factor_required && managedSubjectInstance.mobileNumber && managedSubjectInstance.resetCodeExternal == null)) {
      if(grailsApplication.config.aaf.vhr.passwordreset.second_factor_required) {
        managedSubjectInstance.resetCodeExternal = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(grailsApplication.config.aaf.vhr.passwordreset.reset_code_length)

        flash.type = 'info'
        flash.message = 'controllers.aaf.vhr.lostpassword.reset.sent.externalcode'
      } else {
        // When second factor is disabled (i.e no SMS such as in the test federation) do it over email.
        managedSubjectInstance.resetCode = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(grailsApplication.config.aaf.vhr.passwordreset.reset_code_length)

        flash.type = 'info'
        flash.message = 'controllers.aaf.vhr.lostpassword.reset.sent.email'
      }
      sendResetCodes(managedSubjectInstance)
    }

    def groupRole = Role.findWhere(name:"group:${managedSubjectInstance.group.id}:administrators")
    def organizationRole = Role.findWhere(name:"organization:${managedSubjectInstance.organization.id}:administrators")
    def allowResend = !grailsApplication.config.aaf.vhr.passwordreset.second_factor_required || managedSubjectInstance.mobileNumber

    [managedSubjectInstance:managedSubjectInstance, groupRole:groupRole, organizationRole:organizationRole, allowResend:allowResend]
  }

  def resend() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    use(TimeCategory) {
      def t = managedSubjectInstance.lastCodeResend
      if (t && t > 4.minutes.ago) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.lostpassword.resend.error'
      } else {
        sendResetCodes(managedSubjectInstance)

        managedSubjectInstance.lastCodeResend = new Date()
        managedSubjectInstance.save()

        flash.type = 'info'
        flash.message = 'controllers.aaf.vhr.lostpassword.resend.success'
      }
    }

    redirect action: 'reset'
  }

  def validatereset() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    if(grailsApplication.config.aaf.vhr.passwordreset.second_factor_required) {
      if(managedSubjectInstance.resetCodeExternal != params.resetCodeExternal || managedSubjectInstance.resetCodeExternal == null) {
        managedSubjectInstance.increaseFailedResets()

        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.lostpassword.externalcode.error'
        redirect action: 'reset'
        return
      }
    } else {
      // When second factor is disabled (i.e no SMS such as in the test federation) validate email code.
      if(managedSubjectInstance.resetCode != params.resetCode || managedSubjectInstance.resetCode == null) {
        managedSubjectInstance.increaseFailedResets()

        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.lostpassword.emailcode.error'
        redirect action: 'reset'
        return
      }
    }

    managedSubjectInstance.plainPassword = params.plainPassword
    managedSubjectInstance.plainPasswordConfirmation = params.plainPasswordConfirmation

    def (validPassword, errors) = passwordValidationService.validate(managedSubjectInstance)
    if(!validPassword) {
      log.error "New password is invalid for $managedSubjectInstance"
      
      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.lostpassword.validatereset.new.password.invalid'

      def groupRole = Role.findWhere(name:"group:${managedSubjectInstance.group.id}:administrators")
      def organizationRole = Role.findWhere(name:"organization:${managedSubjectInstance.organization.id}:administrators")

      render view: 'reset', model:[managedSubjectInstance:managedSubjectInstance, groupRole:groupRole, organizationRole:organizationRole]
      return
    }

    cryptoService.generatePasswordHash(managedSubjectInstance)
    String reason = "User provided correct " + (grailsApplication.config.aaf.vhr.passwordreset.second_factor_required ? " external" : "") + " reset code."
    String requestDetails = createRequestDetails(request)

    managedSubjectInstance.successfulLostPassword(reason, 'password_reset', requestDetails, null)

    def deprecatedSubject = DeprecatedSubject.findWhere(login:managedSubjectInstance.login, migrated:false)
    if(deprecatedSubject) {
      deprecatedSubject.migrated = true 
      deprecatedSubject.save()
    }

    log.error "Successful LostPassword reset for $managedSubjectInstance"

    session.removeAttribute(CURRENT_USER)

    flash.type = 'success'
    flash.message = 'controllers.aaf.vhr.lostpassword.validatereset.new.password.success'
    redirect action: 'complete'
  }

  def complete() { }

  def unavailable() { }

  def support() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    if(!managedSubjectInstance) {
      log.error "Unable to present account support details as managedSubjectInstance doesn't appear in session."
      redirect action: 'start'
      return
    }

    def groupRole = Role.findWhere(name:"group:${managedSubjectInstance.group.id}:administrators")
    def organizationRole = Role.findWhere(name:"organization:${managedSubjectInstance.organization.id}:administrators")

    [managedSubjectInstance:managedSubjectInstance, organizationRole:organizationRole, groupRole:groupRole]
  }

  def logout() {
    session.removeAttribute(CURRENT_USER)
    redirect controller:'dashboard', action:'welcome'
  }

  private void sendResetCodes(ManagedSubject managedSubjectInstance) {
    if(grailsApplication.config.aaf.vhr.passwordreset.second_factor_required) {
      // SMS reset code (UI asks to contact admin if no mobile)
      if(managedSubjectInstance.mobileNumber) {
        if(!sendsms(managedSubjectInstance)) {
          redirect action: 'unavailable'
          return
        }
      }
    } else {
      // When second factor is disabled (i.e no SMS such as in the test federation) do it over email.
      def emailSubject = messageSource.getMessage(EMAIL_CODE_SUBJECT, [] as Object[], EMAIL_CODE_SUBJECT, LocaleContextHolder.locale)
      def emailTemplate = EmailTemplate.findWhere(name:"email_password_code")
      emailManagerService.send(managedSubjectInstance.email, emailSubject, emailTemplate, [managedSubject:managedSubjectInstance])
    }
  }

  private boolean sendsms(ManagedSubject managedSubjectInstance) {
    def config = grailsApplication.config.aaf.vhr.passwordreset

    String mobileNumber = managedSubjectInstance.mobileNumber
    String text = config.reset_sms_text.replace('{0}', managedSubjectInstance.resetCodeExternal)
    smsDeliveryService.send(mobileNumber, text)
  }

  private String createRequestDetails(def request) {
"""User Agent: ${request.getHeader('User-Agent')}
Remote Host: ${request.getRemoteHost()}
Remote IP: ${request.getRemoteAddr()}"""
  }

}
