package aaf.vhr.api.v1

import grails.test.mixin.*
import grails.test.spock.*

import spock.lang.*

import aaf.base.identity.*
import aaf.vhr.AuthnTuple

import java.text.SimpleDateFormat
import java.util.TimeZone

import spock.lang.Specification
import grails.testing.web.controllers.ControllerUnitTest

class LoginApiControllerSpec extends Specification implements ControllerUnitTest<LoginApiController> {

  def "confirmsession: receive 410 is there is no such session"() {
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

  def "confirmsession: receive valid json when session is present"() {
    setup:
    def loginService = Mock(aaf.vhr.LoginService)
    controller.loginService = loginService

    params.sessionID = "1234abcd"
    def authnInstant = new Date()
    // SimpleDateFormatter for ISO 8601 timestamps with milliseconds
    def sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    // Set the timezone to UTC - putting initialization into ()
    sdf.setTimeZone(TimeZone.getTimeZone("UTC"))


    when:
    controller.confirmsession()

    then:
    1 * loginService.sessionRemoteUser(params.sessionID) >> new AuthnTuple('testuser', authnInstant, false)
    response.status == 200
    response.contentType == 'application/json;charset=UTF-8'
    response.text == '{"remote_user":"testuser","authnInstant":"'+sdf.format(authnInstant)+'","mfa":false}'
  }

  def "basicauth: receive 410 is there is no such object"() {
    setup:
    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    request.method = 'POST'
    controller.basicauth("testuser", "password")

    then:
    response.status == 410
  }

  def "basicauth: receive 403 if account not functioning"() {
    setup:
    def ms = new aaf.vhr.ManagedSubject(active:false)
    ms.organization.active = true

    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    request.method = 'POST'
    controller.basicauth(ms.login, "password")

    then:
    0 * cryptoService.verifyPasswordHash("password", ms)
    response.status == 403
  }

  def "basicauth: receive 403 if invalid credential supplied"() {
    setup:
    def ms = new aaf.vhr.ManagedSubject(active:true)
    ms.organization.active = true

    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    request.method = 'POST'
    controller.basicauth(ms.login, "password")

    then:
    1 * cryptoService.verifyPasswordHash("password", ms) >> false
    response.status == 403
  }

  def "basicauth: receive 200 if valid credential supplied"() {
    setup:
    def ms = new aaf.vhr.ManagedSubject(active:true)
    ms.organization.active = true

    def cryptoService = Mock(aaf.vhr.CryptoService)
    controller.cryptoService = cryptoService

    when:
    request.method = 'POST'
    controller.basicauth(ms.login, "password")

    then:
    1 * cryptoService.verifyPasswordHash("password", ms) >> true
    response.status == 200
  }

}
