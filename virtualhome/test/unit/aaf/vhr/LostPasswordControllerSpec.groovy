package aaf.vhr

import grails.test.mixin.*
import grails.test.spock.*
import grails.buildtestdata.mixin.Build

import spock.lang.*

import aaf.base.identity.*
import aaf.base.SMSDeliveryService

@TestFor(aaf.vhr.LostPasswordController)
@Build([aaf.vhr.Organization, aaf.vhr.Group, aaf.vhr.ManagedSubject, aaf.base.admin.EmailTemplate, aaf.base.identity.Subject, aaf.base.identity.Role, aaf.vhr.switchch.vho.DeprecatedSubject])
@Mock([Organization, Group, StateChange])
class LostPasswordControllerSpec extends spock.lang.Specification {

  def passwordValidationService
  def cryptoService
  def emailManagerService
  def smsDeliveryService

  def setup() {
    passwordValidationService = Mock(aaf.vhr.PasswordValidationService)
    controller.passwordValidationService = passwordValidationService

    cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    emailManagerService = Mock(aaf.base.EmailManagerService)
    controller.emailManagerService = emailManagerService

    smsDeliveryService = Mock(SMSDeliveryService)
    controller.smsDeliveryService = smsDeliveryService
  }

  def 'validManagedSubjectInstance errors if no managedsubject in session'() {
    when:
    def result = controller.validManagedSubjectInstance()

    then:
    !result
    flash.type == 'info'
    flash.message == 'controllers.aaf.vhr.lostpassword.requiresaccount'
    response.redirectedUrl == "/lostPassword/start"
  }

  def 'validManagedSubjectInstance errors if locked managedsubject in session'() {
    setup:
    grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit = 5
    
    def ms = ManagedSubject.build(locked:true)
    session.setAttribute(controller.CURRENT_USER, ms.id)

    when:
    def result = controller.validManagedSubjectInstance()

    then:
    !result
    response.redirectedUrl == "/lostPassword/support"
  }

  def 'validManagedSubjectInstance errors if managedsubject in session has met failed attempts amount'() {
    setup:
    grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit = 5

    def ms = ManagedSubject.build(locked:false, failedResets:5)
    ms.organization.active = true
    session.setAttribute(controller.CURRENT_USER, ms.id)

    when:
    def result = controller.validManagedSubjectInstance()

    then:
    !result
    ms.stateChanges.size() == 1
    ms.stateChanges.toArray()[0].reason == "Locked by forgotten password process due to many failed login attempts"

    response.redirectedUrl == "/lostPassword/support"
  }

  def 'validManagedSubjectInstance succeeds if valid managedsubject in session'() {
    setup:
    grailsApplication.config.aaf.vhr.passwordreset.reset_attempt_limit = 5

    def ms = ManagedSubject.build(locked:false, failedResets:2)
    ms.organization.active = true
    session.setAttribute(controller.CURRENT_USER, ms.id)

    when:
    def result = controller.validManagedSubjectInstance()

    then:
    result
  }

  def 'obtainsubject errors if no secret code is in the URL parameters'() {
    when:
    request.method = 'GET'
    controller.obtainsubject()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.lostpassword.reset.url.badsecret'
    response.redirectedUrl == "/lostPassword/start"
  }

  def 'obtainsubject errors if no subject has been assigned the secret code from the URL parameters'() {
    setup:
    params.code = 'ABC'
    def ms = ManagedSubject.build(resetCode: 'DEF')

    when:
    request.method = 'GET'
    controller.obtainsubject()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.lostpassword.reset.url.badsecret'
    response.redirectedUrl == "/lostPassword/start"
  }

  def 'obtainsubject allows users with the correct secret code to access the reset page'() {
    setup:
    def secret = 'ABC'
    params.code = secret
    def ms = ManagedSubject.build(resetCode: secret)

    when:
    request.method = 'GET'
    controller.obtainsubject()

    then:
    response.redirectedUrl == "/lostPassword/reset"
  }

  def 'reset does not sms if no mobileNumber'() {
    setup:
    def ms = ManagedSubject.build()
    session.setAttribute(controller.CURRENT_USER, ms.id)

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true
    grailsApplication.config.aaf.vhr.passwordreset.reset_code_length = 6
    grailsApplication.config.aaf.vhr.passwordreset.reset_sms_text = '{0}'

    Role.build(name:"group:${ms.group.id}:administrators")
    Role.build(name:"organization:${ms.organization.id}:administrators")

    expect:
    ms.resetCode == null

    when:
    def model = controller.reset()

    then:
    0 * emailManagerService.send(ms.email, _, _, [managedSubject:ms])
    0 * smsDeliveryService.send(_,_)

    model.managedSubjectInstance == ms
    model.groupRole
    model.organizationRole
  }

  def 'reset does not send email if no mobileNumber and no externalcode'() {
    setup:
    def ms = ManagedSubject.build(resetCode: '123456')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true
    grailsApplication.config.aaf.vhr.passwordreset.reset_code_length = 6
    grailsApplication.config.aaf.vhr.passwordreset.reset_sms_text = '{0}'

    Role.build(name:"group:${ms.group.id}:administrators")
    Role.build(name:"organization:${ms.organization.id}:administrators")

    expect:
    ms.resetCode != null
    ms.resetCodeExternal == null
    ms.mobileNumber == null

    when:
    def model = controller.reset()

    then:
    0 * emailManagerService.send(ms.email, _, _, [managedSubject:ms])
    0 * smsDeliveryService.send(_,_)
  }

  def 'reset does send sms if mobileNumber but redirects to unavailable on fault'() {
    setup:
    def ms = ManagedSubject.build(mobileNumber:'+61413234567')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true
    grailsApplication.config.aaf.vhr.passwordreset.reset_code_length = 6

    expect:
    ms.resetCode == null
    ms.resetCodeExternal == null

    when:
    def model = controller.reset()

    then:
    0 * emailManagerService.send(ms.email, _, _, [managedSubject:ms])
    1 * smsDeliveryService.send(_,_) >> false
    ms.resetCode == null
    ms.resetCodeExternal.length() == 6

    response.redirectedUrl == "/lostPassword/unavailable"
  }

  def 'reset does send sms if mobileNumber'() {
    setup:
    def ms = ManagedSubject.build(mobileNumber:'+61413234567')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true
    grailsApplication.config.aaf.vhr.passwordreset.reset_code_length = 6

    Role.build(name:"group:${ms.group.id}:administrators")
    Role.build(name:"organization:${ms.organization.id}:administrators")

    expect:
    ms.resetCode == null
    ms.resetCodeExternal == null

    when:
    def model = controller.reset()

    then:
    0 * emailManagerService.send(ms.email, _, _, [managedSubject:ms])
    1 * smsDeliveryService.send(ms.mobileNumber, _ as String) >> true
    ms.resetCode == null
    ms.resetCodeExternal.length() == 6

    model.managedSubjectInstance == ms
    model.groupRole
    model.organizationRole
  }

  def 'resend sets resend time'() {
    setup:
    def ms = ManagedSubject.build(resetCode:'1234')
    session.setAttribute(controller.CURRENT_USER, ms.id)
    def start = new Date()

    expect:
    ms.lastCodeResend == null

    when:
    controller.resend()

    then:
    ms.lastCodeResend >= start
  }

  def 'resend sends the same codes again'() {
    setup:
    def ms = ManagedSubject.build(resetCode:'1234')
    session.setAttribute(controller.CURRENT_USER, ms.id)
    def start = new Date()

    expect:
    ms.lastCodeResend == null

    when:
    controller.resend()

    then:
    0 * emailManagerService.send(ms.email, _, _, [managedSubject:ms])
    ms.resetCode == '1234'
  }

  def 'resend refuses to send codes again too quickly'() {
    setup:
    def ms = ManagedSubject.build(resetCode:'1234', lastCodeResend: new Date())
    session.setAttribute(controller.CURRENT_USER, ms.id)

    when:
    controller.resend()

    then:
    0 * emailManagerService._
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.lostpassword.resend.error'
    response.status == 302
  }

  def 'validatereset increases failure count if resetCodes do not match'() {
    setup:
    def ms = ManagedSubject.build(resetCodeExternal:'1234', mobileNumber:'+61413234567')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    params.resetCodeExternal = '5678'

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true

    expect:
    ms.failedResets == 0

    when:
    request.method = 'POST'
    controller.validatereset()

    then:
    ms.failedResets == 1
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.lostpassword.externalcode.error'
    response.redirectedUrl == "/lostPassword/reset"
  }

  def 'validatereset increases failure count if resetCodeExternal does not match and second_factor_required'() {
    setup:
    def ms = ManagedSubject.build(resetCode:'1234', resetCodeExternal:'5678', mobileNumber:'+61413234567')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    params.resetCode = '1234'
    params.resetCodeExternal = 'abcd'

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true

    expect:
    ms.failedResets == 0

    when:
    request.method = 'POST'
    controller.validatereset()

    then:
    ms.failedResets == 1
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.lostpassword.externalcode.error'
    response.redirectedUrl == "/lostPassword/reset"
  }

  def 'validatereset does not increase counts but fails to pass on password format error'() {
    setup:
    def ms = ManagedSubject.build(resetCode:'1234', resetCodeExternal:'5678')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    Role.build(name:"group:${ms.group.id}:administrators")
    Role.build(name:"organization:${ms.organization.id}:administrators")

    params.resetCode = '1234'
    params.resetCodeExternal = '5678'

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true

    expect:
    ms.failedResets == 0

    when:
    request.method = 'POST'
    controller.validatereset()

    then:
    ms.failedResets == 0
    1 * passwordValidationService.validate(_) >>> [[false, null]]
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.lostpassword.validatereset.new.password.invalid'
    view == '/lostPassword/reset'
    model.managedSubjectInstance == ms
    model.groupRole
    model.organizationRole
  }

  def 'validatereset completes successfully with correct codes and valid password'() {
    setup:
    def ms = ManagedSubject.build(active:false, failedResets:1, failedLogins:2, resetCode:'1234', resetCodeExternal:'5678')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    Role.build(name:"group:${ms.group.id}:administrators")
    Role.build(name:"organization:${ms.organization.id}:administrators")

    params.resetCode = '1234'
    params.resetCodeExternal = '5678'

    grailsApplication.config.aaf.vhr.passwordreset.second_factor_required = true

    expect:
    ms.failedResets == 1
    !ms.active

    when:
    request.method = 'POST'
    controller.validatereset()

    then:
    1 * passwordValidationService.validate(_) >>> [[true, null]]
    1 * cryptoService.generatePasswordHash(ms)

    ms.active
    ms.failedLogins == 0
    ms.resetCode == null
    ms.resetCodeExternal == null
    ms.failedResets == 0

    session.getAttribute(controller.CURRENT_USER) == null

    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.lostpassword.validatereset.new.password.success'
    response.redirectedUrl == "/lostPassword/complete"
  }

  def 'ensure support reverts to start if no session object'() {
    when:
    controller.support()

    then:
    response.redirectedUrl == "/lostPassword/start"
  }

  def 'ensure correct functioning of support'() {
    setup:
    def ms = ManagedSubject.build(failedResets:1, resetCode:'1234', resetCodeExternal:'5678')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    def gr = Role.build(name:"group:${ms.group.id}:administrators")
    def or = Role.build(name:"organization:${ms.organization.id}:administrators")

    when:
    def model = controller.support()

    then:
    model.managedSubjectInstance == ms
    model.groupRole == gr
    model.organizationRole == or
  }

  def 'ensure logout invalidates session and redirects'() {
    setup:
    def ms = ManagedSubject.build(failedResets:1, resetCode:'1234', resetCodeExternal:'5678')
    session.setAttribute(controller.CURRENT_USER, ms.id)

    expect:
    session.getAttribute(controller.CURRENT_USER) == ms.id

    when:
    controller.logout()

    then:
    session.getAttribute(controller.CURRENT_USER) == null
    response.redirectedUrl == "/dashboard/welcome"
  }

}
