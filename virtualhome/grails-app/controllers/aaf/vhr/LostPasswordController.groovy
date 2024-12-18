package aaf.vhr

import groovy.time.TimeCategory
import org.springframework.context.i18n.LocaleContextHolder

import aaf.base.identity.Role
import aaf.base.admin.EmailTemplate
import aaf.vhr.switchch.vho.DeprecatedSubject

class LostPasswordController {

  static allowedMethods = [emailed: 'POST', validatereset: 'POST']

  final String CURRENT_USER = "aaf.vhr.LostPasswordController.CURRENT_USER"
  final String EMAIL_CODE_SUBJECT ='controllers.aaf.vhr.lostpassword.email.code.subject'

  final static defaultAction = "start"

  def messageSource
  def passwordValidationService
  def cryptoService
  def emailManagerService
  def smsDeliveryService
  def lostPasswordService

  def beforeInterceptor = [action: this.&validManagedSubjectInstance, except: ['start', 'emailed', 'obtainsubject', 'complete', 'unavailable', 'support', 'logout']]

  def start() {
  }

  def emailed() {

    if (!params.login) {
      log.error "No username was specified! We can't perform any lookup!"
      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.lostpassword.requiresaccount'
      redirect action: 'start'
      return
    }

    log.info "User ${params.login} has requested a password reset!"

    def managedSubjectInstance = ManagedSubject.findWhere(login: params.login)
    if (managedSubjectInstance) {
      lostPasswordService.sendResetEmail(managedSubjectInstance)
    }

    [login: params.login]
  }

  def obtainsubject() {
    // Check that the clicked link is for an emailed user by checking their resetCode parameter

    def resetCode = params.code
    if (!resetCode) {
      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.lostpassword.reset.url.badsecret'
      redirect action: 'start'
      return
    }

    def managedSubjectInstance = ManagedSubject.findWhere(resetCode: resetCode)
    if (!managedSubjectInstance) {
      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.lostpassword.reset.url.badsecret'
      redirect action: 'start'
      return
    }

    session.setAttribute(CURRENT_USER, managedSubjectInstance.id)

    // If we haven't generated an SMS code already, generate an SMS code and sent it to the user (even if we have already sent one)
    def smsCode = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(grailsApplication.config.aaf.vhr.passwordreset.reset_code_length)
    managedSubjectInstance.resetCodeExternal = smsCode
    flash.type = 'info'
    flash.message = 'controllers.aaf.vhr.lostpassword.reset.sent.externalcode'
    sendResetCodes(managedSubjectInstance)

    redirect action: 'reset'
  }

  def reset() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    def groupRole = Role.findWhere(name:"group:${managedSubjectInstance.group.id}:administrators")
    def organizationRole = Role.findWhere(name:"organization:${managedSubjectInstance.organization.id}:administrators")
    def allowResend = managedSubjectInstance.mobileNumber

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

    if(managedSubjectInstance.resetCodeExternal != params.resetCodeExternal || managedSubjectInstance.resetCodeExternal == null) {
      managedSubjectInstance.increaseFailedResets()

      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.lostpassword.externalcode.error'
      redirect action: 'reset'
      return
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
    String reason = "User provided correct external reset code."
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

  private boolean validManagedSubjectInstance() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    if(!managedSubjectInstance) {
      log.error "No ManagedSubject stored in session, requesting login before accessing password change"
      
      flash.type = 'info'
      flash.message = 'controllers.aaf.vhr.lostpassword.requiresaccount'
      redirect action: 'start'
      return false
    }

    if(!managedSubjectInstance.canChangePassword() || managedSubjectInstance.failedResets >= grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit.intValue()) {
      if(managedSubjectInstance.failedResets >= grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit.intValue()) {
        String reason = "Locked by forgotten password process due to many failed login attempts"
        String requestDetails = """
User Agent: ${request.getHeader('User-Agent')}
Remote Host: ${request.getRemoteHost()}
Remote IP: ${request.getRemoteAddr()}"""

        managedSubjectInstance.lock(reason, 'lost_password_max_attempts_reached', requestDetails, null)
      }

      redirect action: 'support'
      return false
    }

    true
  }

  private void sendResetCodes(ManagedSubject managedSubjectInstance) {
    // SMS reset code (UI asks to contact admin if no mobile)
    if(managedSubjectInstance.mobileNumber) {
      if(!sendsms(managedSubjectInstance)) {
        redirect action: 'unavailable'
        return
      }
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
