package aaf.vhr

import grails.test.mixin.*
import grails.test.spock.*

import aaf.vhr.crypto.GoogleAuthenticator

import spock.lang.*

import aaf.base.identity.*
import grails.testing.web.controllers.ControllerUnitTest

class AccountControllerSpec extends Specification implements ControllerUnitTest<AccountController> {
  //def grailsApplication = new grails.core.DefaultGrailsApplication()

  def setup() {
  }

  def 'ensure index provides a view'() {
    when:
    controller.index()

    then:
    true
  }

  def "failed login renders to index"() {
    setup:
    def loginService = Mock(aaf.vhr.LoginService)

    def ms = new ManagedSubject(active:true, failedLogins: 0)
    ms.organization.active = true

    controller.loginService = loginService

    when:
    request.method = 'POST'
    controller.login(ms.login, 'password')

    then:
    1 * loginService.passwordLogin(ms, _, _, _, _) >> false
    view == '/account/index'
  }

  def "successful login redirect to show"() {
    setup:
    def loginService = Mock(aaf.vhr.LoginService)

    def ms = new ManagedSubject(active:true, failedLogins: 0)
    ms.organization.active = true

    controller.loginService = loginService

    when:
    request.method = 'POST'
    controller.login(ms.login, 'password')

    then:
    1 * loginService.passwordLogin(ms, _, _, _, _) >> true
    response.redirectedUrl == "/account/show"
  }

  def 'ensure logout invalidate session'() {
    setup:
    session.setAttribute('aaf.vhr.AccountController.CURRENT_USER', 'value')

    expect:
    session.getAttribute('aaf.vhr.AccountController.CURRENT_USER') == 'value'

    when:
    controller.logout()

    then:
    session.getAttribute('aaf.vhr.AccountController.CURRENT_USER') == null
    response.redirectedUrl == "/dashboard/welcome"
  }

  def 'show without session ManagedSubjectInstance directs to index'(){
    when:
    controller.show()

    then:
    response.redirectedUrl == "/account/index"
  }

  def 'show with exisiting session stored subject renders view'() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)

    def managedSubjectTestInstance = new ManagedSubject(login:'validlogin')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)

    controller.cryptoService = cryptoService

    when:
    def model = controller.show()

    then:
    0 * cryptoService.verifyPasswordHash(_ as String, _ as ManagedSubject) >>> true
    response.status == 200
    model.managedSubjectInstance == managedSubjectTestInstance
  }

  def 'changedetails with no existing login requires login'() {
    when:
    controller.changedetails()

    then:
    flash.type == 'info'
    flash.message == 'controllers.aaf.vhr.account.changedetails.requireslogin'
    response.redirectedUrl == '/account/index'
  }

  def 'changedetails with existing login succeeds'() {
    setup:
    def managedSubjectTestInstance = new ManagedSubject(login:'validlogin')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)

    when:
    def model = controller.changedetails()

    then:
    response.status == 200
    model.managedSubjectInstance == managedSubjectTestInstance
  }

  def 'completedetailschange with no existing login requires login'() {
    when:
    request.method = 'POST'
    controller.completedetailschange()

    then:
    response.status == 403
  }

  def 'completedetailschange with existing login but wrong password fails'() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)

    def managedSubjectTestInstance = new ManagedSubject(login:'validlogin')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)
    params.currentPassword = 'password'

    controller.cryptoService = cryptoService

    when:
    request.method = 'POST'
    controller.completedetailschange()

    then:
    1 * cryptoService.verifyPasswordHash(_ as String, _ as ManagedSubject) >>> false

    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.account.completedetailschange.password.error'
    view == '/account/changedetails'
    model.managedSubjectInstance == managedSubjectTestInstance
  }

  def 'completedetailschange with existing login, correct current password but invalid new password fails'() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)
    def passwordValidationService = Mock(aaf.vhr.PasswordValidationService)

    def managedSubjectTestInstance = new ManagedSubject(login:'validlogin')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)

    params.currentPassword = 'password'
    params.plainPassword = 'newpassword'
    params.plainPasswordConfirmation = 'newpassword'

    controller.passwordValidationService = passwordValidationService
    controller.cryptoService = cryptoService

    when:
    request.method = 'POST'
    controller.completedetailschange()

    then:
    1 * cryptoService.verifyPasswordHash(_ as String, _ as ManagedSubject) >>> true
    1 * passwordValidationService.validate(_ as ManagedSubject) >>> [[false, ['some.error', 'some.other.error']]]

    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.account.completedetailschange.new.password.invalid'
    view == '/account/changedetails'
    model.managedSubjectInstance == managedSubjectTestInstance
  }

  def 'completedetailschange with existing login, correct current password and valid new password succeeds'() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)
    def passwordValidationService = Mock(aaf.vhr.PasswordValidationService)

    def managedSubjectTestInstance = new ManagedSubject(login:'validlogin')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)

    params.currentPassword = 'password'
    params.plainPassword = 'newpassword'
    params.plainPasswordConfirmation = 'newpassword'

    controller.cryptoService = cryptoService
    controller.passwordValidationService = passwordValidationService

    when:
    request.method = 'POST'
    controller.completedetailschange()

    then:
    1 * cryptoService.verifyPasswordHash(_ as String, _ as ManagedSubject) >>> true
    1 * cryptoService.generatePasswordHash(_ as ManagedSubject)
    1 * passwordValidationService.validate(_ as ManagedSubject) >>> [[true, []]]

    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.account.completedetailschange.new.password.success'
    response.redirectedUrl == '/account/show'
  }

  def 'completedetailschange updates mobile number when valid'() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    def managedSubjectTestInstance = new ManagedSubject(login: 'validlogin', mobileNumber: '+61487654321')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)

    when:
    params.currentPassword = 'password'
    params.mobileNumber = '+61412345678'
    request.method = 'POST'
    controller.completedetailschange()

    then:
    1 * cryptoService.verifyPasswordHash(_, _) >> true

    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.account.completedetailschange.success'

    managedSubjectTestInstance.mobileNumber == '+61412345678'
  }

  def 'ensure enabletwostep generates totpkey'() {
    setup:
    def managedSubjectTestInstance = new ManagedSubject(login: 'validlogin', mobileNumber: '+61487654321')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)

    when:
    controller.enabletwostep()

    then:
    response.status == 200
    session.getAttribute(controller.NEW_TOTP_KEY) != null
  }

  def 'ensure finishenablingtwostep fails with invalid code'() {
    setup:
    def managedSubjectTestInstance = new ManagedSubject(login: 'validlogin', mobileNumber: '+61487654321')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)
    session.setAttribute(controller.NEW_TOTP_KEY, "0")

    GoogleAuthenticator.metaClass.static.checkCode = { String key, long code, long time -> false }

    expect:
    managedSubjectTestInstance.totpKey == null

    when:
    params.totp = 1
    request.method = 'POST'
    controller.finishenablingtwostep()

    then:
    response.status == 200
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.account.finish.twostep.error'
  }

  def 'ensure finishenablingtwostep succeeds with valid code'() {
    setup:
    def managedSubjectTestInstance = new ManagedSubject(login: 'validlogin', mobileNumber: '+61487654321')
    session.setAttribute(controller.CURRENT_USER, managedSubjectTestInstance.id)
    session.setAttribute(controller.NEW_TOTP_KEY, "0")

    GoogleAuthenticator.metaClass.static.checkCode = { String key, long code, long time -> true }

    expect:
    managedSubjectTestInstance.totpKey == null

    when:
    params.totp = 1
    request.method = 'POST'
    controller.finishenablingtwostep()

    then:
    response.status == 302
    managedSubjectTestInstance.stateChanges.size() == 1
    managedSubjectTestInstance.stateChanges.toArray()[0].event == StateChangeType.SETUPTWOSTEP
    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.account.finish.twostep.success'
  }

}
