package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import org.apache.shiro.authc.*

import grails.util.Holders

import test.shared.ShiroEnvironment

@TestFor(aaf.base.identity.AuthController)
class AuthControllerSpec extends spock.lang.Specification {

  @Shared def shiroEnvironment = new ShiroEnvironment()
  
  def cleanupSpec() { 
    shiroEnvironment.tearDownShiro() 
  }

  def 'Echo returns set attributes'() {   
    setup:
    grailsApplication.config.aaf.base.realms.federated = [
      request: [attributes: true]
    ]
    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    
    request.setAttribute('Shib-Entity-ID', 'http://test.com/idpshibboleth') 
    request.setAttribute('displayName', 'Joe Bloggs')
    
    when:
    def model = controller.echo()
    
    then:
    model.attr.'Shib-Entity-ID' == "http://test.com/idpshibboleth"
    model.attr.displayName == "Joe Bloggs"
  }
  
  def 'Echo returns set headers'() {    
    setup:
    grailsApplication.config.aaf.base.realms.federated = [
      request: [attributes: false]
    ]
    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    
    request.addHeader('Shib-Entity-ID', 'http://test.com/idpshibboleth') 
    request.addHeader('displayName', 'Joe Bloggs')
    
    when:
    def model = controller.echo()
    
    then:
    model.attr == ['Shib-Entity-ID':"http://test.com/idpshibboleth", displayName:"Joe Bloggs"]
  }

  def 'federatederror'() {
    when:
    controller.federatederror()
 
    then:
    true
  }

  def 'Poll responds with default text'() {
    when:
    controller.poll()
 
    then:
    response.text == "Polled webserver successfully"
  }

  def 'that login view will be rendered when automate login is disabled'() {
    setup:
    def linkAction
    def linkAbsolute

    grailsApplication.config.aaf.base.realms.federated = [
      automate_login: false,
      sso_endpoint: "/Shibboleth.sso/Login"
    ]

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}

    controller.metaClass.createLink = { attrs ->
      linkAction = attrs.action
      linkAbsolute = attrs.absolute
      "http://test.com/federatedlogin"
    }
    
    when:
    def result = controller.login()
    
    then:
    response.status == 200
    linkAction == 'federatedlogin'
    linkAbsolute
    result.spsession_url == "/Shibboleth.sso/Login?target=http://test.com/federatedlogin"
  }
  
  def 'that SP redirect will be invoked when autologin active'() {
    setup:
    def linkAction
    def linkAbsolute
    
    grailsApplication.config.aaf.base.realms.federated = [
      automate_login: true,
      sso_endpoint: "/Shibboleth.sso/Login"
    ]

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    
    controller.metaClass.createLink = { attrs ->
      linkAction = attrs.action
      linkAbsolute = attrs.absolute
      "http://test.com/federatedlogin"
    }
    
    params.targetUri = '/some/test/content'
    
    when:
    controller.login()
    
    then:
    linkAction == 'federatedlogin'
    linkAbsolute
    response.redirectedUrl == '/Shibboleth.sso/Login?target=http://test.com/federatedlogin'
    session[AuthController.TARGET] == '/some/test/content'
  }
  
  def 'that logout will redirect to application welcome'() {   
    setup:
    def subject = Mock(org.apache.shiro.subject.Subject)
    subject.isAuthenticated() >> true
    shiroEnvironment.setSubject(subject)
    
    controller.metaClass.getSubject = { [id:1, principal:'http://test.com!http://sp.test.com!1234'] }
    
    when:
    controller.logout()
    
    then:
    response.redirectedUrl == '/dashboard/welcome'
  }

  def 'that logout will redirect to target URI if present'() {   
    setup:
    def subject = Mock(org.apache.shiro.subject.Subject)
    subject.isAuthenticated() >> true
    shiroEnvironment.setSubject(subject)
    
    controller.metaClass.getSubject = { [id:1, principal:'http://test.com!http://sp.test.com!1234'] }
    params.targetUri = "/test-uri"
    
    when:
    controller.logout()
    
    then:
    response.redirectedUrl == '/test-uri'
  }

}
