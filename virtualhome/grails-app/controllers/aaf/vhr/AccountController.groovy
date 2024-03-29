package aaf.vhr

import groovy.time.TimeCategory
import aaf.base.identity.SessionRecord

import aaf.base.identity.Role
import aaf.vhr.switchch.vho.DeprecatedSubject
import aaf.vhr.MigrateController

import aaf.vhr.crypto.GoogleAuthenticator

class AccountController {

  static allowedMethods = [login: 'POST', twosteplogin:'POST', finishenablingtwostep: 'POST', completedetailschange: 'POST']

  static final CURRENT_USER = "aaf.vhr.AccountController.CURRENT_USER"
  static final INVALID_USER = "aaf.vhr.AccountController.INVALID_USER"
  static final NEW_TOTP_KEY = "aaf.vhr.AccountController.NEW_TOTP_KEY"

  def loginService
  def cryptoService
  def passwordValidationService
  def grailsApplication

  def index() {
  }

  def login(String username, String password) {
    def deprecatedSubject = username != null ? DeprecatedSubject.findWhere(login:username, migrated:false) : null
    if(deprecatedSubject) {
      session.setAttribute(MigrateController.MIGRATION_USER, username)
      redirect (controller:'migrate', action:'introduction')
      return
    }

    def managedSubjectInstance = username != null ? ManagedSubject.findWhere(login: username, [lock:true]) : null
    if(!managedSubjectInstance) {
      log.error "No such ManagedSubject for ${params.login} when attempting myaccount login"

      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.account.login.error'
      render view: 'index', model:[loginError:true]

      return
    }

    def validPassword = loginService.passwordLogin(managedSubjectInstance, password, request, session, params)

    if(!validPassword) {
      log.info "LoginService indicates failure for password login by $managedSubjectInstance to myaccount"
      session.setAttribute(CURRENT_USER, managedSubjectInstance.id)
      def failedCaptcha = managedSubjectInstance.stateChanges?.sort{it.dateCreated}?.last()?.event == StateChangeType.FAILCAPTCHA
      render view:'index', model:[loginError: !failedCaptcha, loginWarning: failedCaptcha, requiresChallenge:managedSubjectInstance.requiresLoginCaptcha()]
      return
    }

    if(managedSubjectInstance.isUsingTwoStepLogin()){
      render(view: "twostep", model: [managedSubjectInstance: managedSubjectInstance])
      return
    }

    session.setAttribute(CURRENT_USER, managedSubjectInstance.id)
    redirect action:'show'
  }

  def twosteplogin(long id, long totp) {
    def managedSubjectInstance = ManagedSubject.get(id)
    if(!managedSubjectInstance) {
      log.error "No ManagedSubject represented by $id in extendedlogin"
      session.setAttribute(INVALID_USER, true)
      redirect action:"index"
      return
    }

    if(!loginService.twoStepLogin(managedSubjectInstance, totp, request, response)) {
      log.info "LoginService indicates 2-Step verification failure for attempted login by $managedSubjectInstance"
      render(view: "twostep", model: [managedSubjectInstance: managedSubjectInstance, loginError:true])
      return
    }

    log.info("Verified that 2Step code for ${managedSubjectInstance} is valid, establishing new session.")

    session.setAttribute(CURRENT_USER, managedSubjectInstance.id)
    redirect action:'show'
  }

  def logout() {
    session.removeAttribute(CURRENT_USER)
    session.removeAttribute(INVALID_USER)
    session.removeAttribute(NEW_TOTP_KEY)
    session.removeAttribute(MigrateController.MIGRATION_USER)
    redirect controller:'dashboard', action:'welcome'
  }

  def show() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    if(!managedSubjectInstance) {
      redirect action:'index'
      return
    }

    def groupRole = Role.findWhere(name:"group:${managedSubjectInstance.group.id}:administrators")
    def organizationRole = Role.findWhere(name:"organization:${managedSubjectInstance.organization.id}:administrators")

    [managedSubjectInstance:managedSubjectInstance, groupRole:groupRole, organizationRole:organizationRole]
  }

  def changedetails() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))

    if(!managedSubjectInstance) {
      log.error "No ManagedSubject stored in session, requesting login before accessing details change"

      flash.type = 'info'
      flash.message = 'controllers.aaf.vhr.account.changedetails.requireslogin'
      redirect action: 'index'

      return
    }

    [managedSubjectInstance:managedSubjectInstance]
  }

  def completedetailschange() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))
    if(!managedSubjectInstance) {
      log.error "A valid session does not already exist to allow completedetailschange to function"
      response.sendError 403
      return
    }

    if(!cryptoService.verifyPasswordHash(params.currentPassword, managedSubjectInstance)) {
      log.error "Password invalid for $managedSubjectInstance"

      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.account.completedetailschange.password.error'
      render view: 'changedetails', model: [managedSubjectInstance:managedSubjectInstance]

      return
    }

    if (params.mobileNumber) {
      managedSubjectInstance.mobileNumber = params.mobileNumber
      if (!managedSubjectInstance.validate()) {
        log.error "New mobile number is invalid for $managedSubjectInstance"

        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.account.completedetailschange.mobileNumber.invalid'
        render view: 'changedetails', model: [managedSubjectInstance: managedSubjectInstance]
        return
      }
    } else {
      managedSubjectInstance.mobileNumber = null
    }
    flash.type = 'success'
    flash.message = 'controllers.aaf.vhr.account.completedetailschange.success'

    if (params.plainPassword || params.plainPasswordConfirmation) {
      managedSubjectInstance.plainPassword = params.plainPassword
      managedSubjectInstance.plainPasswordConfirmation = params.plainPasswordConfirmation

      def (validPassword, errors) = passwordValidationService.validate(managedSubjectInstance)
      if(!validPassword) {
        log.error "New password is invalid for $managedSubjectInstance"

        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.account.completedetailschange.new.password.invalid'
        render view: 'changedetails', model: [managedSubjectInstance:managedSubjectInstance]

        return
      }

      cryptoService.generatePasswordHash(managedSubjectInstance)
      log.info("The account $managedSubjectInstance has successfully changed the account password")
      def change = new StateChange(event:StateChangeType.CHANGEPASSWORD, reason: "User requested password change", category: 'password_change', environment: createRequestDetails(request), actionedBy: null)
      managedSubjectInstance.addToStateChanges(change)
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.account.completedetailschange.new.password.success'
    }

    redirect action: 'show'
  }

  def enabletwostep() {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))
    if(!managedSubjectInstance) {
      log.error "A valid session does not already exist to allow enabletwostep to function"
      response.sendError 403
      return
    }

    def totpKey = GoogleAuthenticator.generateSecretKey()
    session.setAttribute(NEW_TOTP_KEY, totpKey)

    def totpURL = GoogleAuthenticator.getQRBarcodeURL(managedSubjectInstance.login,
                                                      request.serverName, totpKey,
                                                      managedSubjectInstance.encodedTwoStepIssuer)
    [managedSubjectInstance:managedSubjectInstance, totpURL: totpURL]
  }

  def finishenablingtwostep(long totp) {
    def managedSubjectInstance = ManagedSubject.get(session.getAttribute(CURRENT_USER))
    def totpKey = session.getAttribute(NEW_TOTP_KEY)

    if(!managedSubjectInstance || !totpKey) {
      log.error "A valid session does not already exist to allow finishenablingtwostep to function"
      response.sendError 403
      return
    }

    if(GoogleAuthenticator.checkCode(totpKey, totp, System.currentTimeMillis())) {
      managedSubjectInstance.totpKey = totpKey

      def change = new StateChange(event:StateChangeType.SETUPTWOSTEP, reason: "User configured two-step authentication", category: 'two_step_login', environment: createRequestDetails(request), actionedBy: null)
      managedSubjectInstance.addToStateChanges(change)

      if(!managedSubjectInstance.save()) {
        log.error "Unable to persist totpKey for $managedSubjectInstance"
        response.sendError 500
        return
      }

      session.removeAttribute(NEW_TOTP_KEY)

      log.info("The account $managedSubjectInstance entered a valid code when finishing 2-Step setup")
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.account.finish.twostep.success'
      redirect action:'show'
    } else {
      log.warn("The account $managedSubjectInstance entered an invalid code when finishing 2-Step setup")
      flash.type = 'error'
      flash.message = 'controllers.aaf.vhr.account.finish.twostep.error'

      def totpURL = GoogleAuthenticator.getQRBarcodeURL(managedSubjectInstance.login,
                                                        request.serverName, totpKey,
                                                        managedSubjectInstance.encodedTwoStepIssuer)

      render view: 'enabletwostep', model: [managedSubjectInstance:managedSubjectInstance, totpURL: totpURL]
    }
  }

  private String createRequestDetails(def request) {
"""User Agent: ${request.getHeader('User-Agent')}
Remote Host: ${request.getRemoteHost()}
Remote IP: ${request.getRemoteAddr()}"""
  }
}
