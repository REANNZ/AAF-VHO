package aaf.base.admin

import spock.lang.*
import aaf.base.EmailManagerService
import grails.plugin.mail.MailService

class FakeSMSDeliveryControllerSpec extends Specification {
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
