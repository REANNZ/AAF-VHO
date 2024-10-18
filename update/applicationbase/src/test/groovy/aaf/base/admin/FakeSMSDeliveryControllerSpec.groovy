package aaf.base.admin

import spock.lang.*
import aaf.base.EmailManagerService
import grails.plugins.mail.MailService
import grails.testing.web.controllers.ControllerUnitTest

class FakeSMSDeliveryControllerSpec extends Specification implements ControllerUnitTest<FakeSMSDeliveryController> {

  def 'pretend to deliver an sms'() {
    setup:
    grailsApplication.config.aaf.base.sms.fake = true
    def mailService = Mock(MailService)
    controller.mailService = mailService

    when:
    params.putAll(from:'AAF', to:'+61412345678', text:'test message')
    controller.json()

    then:
    1 * mailService.sendMail(_ as Closure)
  }

  def 'pretend not to exist when disabled'() {
    setup:
    grailsApplication.config.aaf.base.sms.fake = false

    when:
    params.putAll(from:'AAF', to:'+61412345678', text:'test message')
    controller.json()

    then:
    response.status == 404
  }
}
