package aaf.base

import groovyx.net.http.*
import static groovyx.net.http.ContentType.*
import static groovyx.net.http.Method.*

import spock.lang.*

import grails.testing.services.ServiceUnitTest

class SMSDeliveryServiceSpec extends Specification implements ServiceUnitTest<SMSDeliveryService> {

  def setup() {
    // Mocking configuration
    grailsApplication.config.aaf.base.sms.api_key = 'api_key'
    grailsApplication.config.aaf.base.sms.api_secret = 'api_secret'
  }

  def 'send the http request'() {
    setup:
    def http = Mock(HTTPBuilder)
    service.metaClass.getHttp = {-> http}
    def data = [uri: [:], response: [:]]

    when:
    service.send('+1234', 'test message')

    then:
    1 * http.request(POST, _ as Closure) >> { arguments ->
      def cl = arguments[1]
      cl.delegate = data
      cl()
    }

    data.uri.path == '/api/1/sms/out'
    data.uri.query instanceof Map
    data.uri.query.to == '1234' // expect leading '+' char removed
    data.uri.query.body == 'test message'
    data.uri.query.userId == 'api_key'
    data.uri.query.password == 'api_secret'
    data.response.success instanceof Closure
    data.response.failure instanceof Closure
  }
}
