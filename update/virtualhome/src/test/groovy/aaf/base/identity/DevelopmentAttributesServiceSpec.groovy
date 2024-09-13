package aaf.base.identity

import spock.lang.*
import grails.util.Holders

@TestFor(DevelopmentAttributesService)
class DevelopmentAttributesServiceSpec extends spock.lang.Specification {
  def setup() {
    grailsApplication.config.aaf.base.realms.federated = [
      active: false,
      development: [ active: true ],

      request: [ attributes: true],

      mapping: [
        principal: 'persistent-id',
        credential: 'Shib-Session-ID'
      ]
    ]

    service.metaClass.getGrailsApplication = { -> [config: Holders.config]}
  }

  def buildRequest() {
    def request = [:]
    request.putAll(
      post: true,

      attributes: [:],
      setAttribute: { k, v ->
        request.attributes[k] = v
      },

      headers: [:],
      setHeader: { k, v ->
        request.headers[k] = v
      }
    )

    request
  }

  def 'get attributes and store them'() {
    setup:
    def request = buildRequest()
    def params = [
      'attributes.persistent-id': 'http://test.com!http://sp.test.com!1234',
      'attributes.Shib-Session-ID': '1234-mockid-5678'
    ]
    def session = [:]

    when:
    service.storeAttributes(request, session, params)

    then:
    !session.isEmpty()
    session[DevelopmentAttributesService.KEY_NAME]['persistent-id'] == 'http://test.com!http://sp.test.com!1234'
    session[DevelopmentAttributesService.KEY_NAME]['Shib-Session-ID'] == '1234-mockid-5678'
  }

  def 'do nothing when development mode is not active'() {
    setup:
    def request = buildRequest()
    def params = [
      'attributes.persistent-id': 'http://test.com!http://sp.test.com!1234',
      'attributes.Shib-Session-ID': '1234-mockid-5678'
    ]
    def session = [:]

    grailsApplication.config.aaf.base.realms.federated.development.active = false

    when:
    service.storeAttributes(request, session, params)

    then:
    session.isEmpty()
  }

  def 'inject the attributes as request attributes'() {
    setup:

    def request = buildRequest()
    def session = [
      (DevelopmentAttributesService.KEY_NAME): [
        'persistent-id': 'http://test.com!http://sp.test.com!1234',
        'Shib-Session-ID': '1234-mockid-5678'
      ]
    ]

    grailsApplication.config.aaf.base.realms.federated.request.attributes = true

    when:
    service.injectAttributes(request, session)

    then:
    !request.attributes.isEmpty()
    request.attributes['persistent-id'] == 'http://test.com!http://sp.test.com!1234'
    request.attributes['Shib-Session-ID'] == '1234-mockid-5678'
  }

  def 'fails to inject the attributes as request headers'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated.request.attributes = false
    def request = buildRequest()
    def session = [:]

    when:
    service.injectAttributes(request, session)

    then:
    thrown(UnsupportedOperationException)
  }

  def "don't inject the attributes when development mode is not active"() {
    setup:
    def request = buildRequest()
    def session = [
      (DevelopmentAttributesService.KEY_NAME): [
        'persistent-id': 'http://test.com!http://sp.test.com!1234',
        'Shib-Session-ID': '1234-mockid-5678'
      ]
    ]

    grailsApplication.config.aaf.base.realms.federated.development.active = false
    grailsApplication.config.aaf.base.realms.federated.request.attributes = true

    when:
    service.injectAttributes(request, session)

    then:
    request.attributes.isEmpty()
  }
}
