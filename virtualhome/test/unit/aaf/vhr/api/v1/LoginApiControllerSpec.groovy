package aaf.vhr.api.v1

import grails.test.mixin.*
import grails.plugin.spock.*
import grails.buildtestdata.mixin.Build

import spock.lang.*

import aaf.base.identity.*

@TestFor(aaf.vhr.api.v1.LoginApiController)
@Build([aaf.vhr.Organization, aaf.vhr.Group, aaf.vhr.ManagedSubject])
@Mock([aaf.vhr.Organization, aaf.vhr.Group, aaf.vhr.ManagedSubject])
class LoginApiControllerSpec extends spock.lang.Specification {

  def "confirmsession: recieve 410 is there is no such session"() {
    setup:
    def loginService = Mock(aaf.vhr.LoginService)
    controller.loginService = loginService

    params.sessionID = "1234abcd"

    when:
    controller.confirmsession()

    then:
    1 * loginService.sessionRemoteUser(params.sessionID) >> null
    response.status == 410
  }

  def "confirmsession: recieve valid json when session is present"() {
    setup:
    def loginService = Mock(aaf.vhr.LoginService)
    controller.loginService = loginService

    params.sessionID = "1234abcd"

    when:
    controller.confirmsession()

    then:
    1 * loginService.sessionRemoteUser(params.sessionID) >> 'testuser'
    response.status == 200
    response.contentType == 'application/json;charset=UTF-8'
    response.text == '{"remote_user":"testuser"}'
  }

  def "basicauth: recieve 410 is there is no such object"() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    controller.basicauth("testuser", "password")

    then:
    response.status == 410
  }

  def "basicauth: recieve 403 if account not functioning"() {
    setup:
    def ms = aaf.vhr.ManagedSubject.build(active:false)
    ms.organization.active = true

    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    controller.basicauth(ms.login, "password")

    then:
    0 * cryptoService.verifyPasswordHash("password", ms)
    response.status == 403
  }

  def "basicauth: recieve 403 if invalid credential supplied"() {
    setup:
    def ms = aaf.vhr.ManagedSubject.build(active:true)
    ms.organization.active = true

    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    controller.basicauth(ms.login, "password")

    then:
    1 * cryptoService.verifyPasswordHash("password", ms) >> false
    response.status == 403
  }

  def "basicauth: recieve 200 if valid credential supplied"() {
    setup:
    def ms = aaf.vhr.ManagedSubject.build(active:true)
    ms.organization.active = true

    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    controller.basicauth(ms.login, "password")

    then:
    1 * cryptoService.verifyPasswordHash("password", ms) >> true
    response.status == 200
  }

}
