package aaf.base

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

class SMSDeliveryService {
  def grailsApplication

  def send(String to, String text) {
    def config = grailsApplication.config.aaf.base.sms
    to = to[1..-1] // Remove leading + character

    boolean outcome = true
    http.request( POST ) {
      uri.path = '/api/1/sms/out'
      uri.query = [userId: config.api_key, password:config.api_secret, to:to, body:text]

      response.success = { resp, reader ->
        log.debug resp.statusLine
        log.info "Sent SMS to $to with successful response of ${resp.status}"
        outcome = true

      }

      response.failure = { resp ->
        log.error "Sent SMS to $to with failure response of ${resp.statusLine.statusCode} : ${resp.statusLine.reasonPhrase}"
      }
    }

    outcome
  }

  def getHttp() { new HTTPBuilder(grailsApplication.config.aaf.base.sms.api_endpoint) }
}
