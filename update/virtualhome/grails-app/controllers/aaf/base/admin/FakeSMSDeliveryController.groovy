package aaf.base.admin

class FakeSMSDeliveryController {
  def grailsApplication
  def mailService

  def json() {
    if (grailsApplication.config.aaf.base.sms.fake) {
      log.info("Fake SMS Controller invoked with $params")

      mailService.sendMail {
        to params.to
        subject 'SMS Delivery'
        from params.from
        body params.text
      }

      render(contentType: 'text/json') {
        [messages: 'Fake SMS sent via mailService. Check GreenMail for output']
      }
    } else {
      log.warn("Fake SMS Controller was invoked, but it has been disabled in application_config.groovy")
      response.sendError(404)
    }
  }
}
