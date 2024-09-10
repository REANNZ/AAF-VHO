package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import org.apache.shiro.authc.*

import grails.util.Holders

import test.shared.ShiroEnvironment

@TestFor(aaf.base.identity.FederatedSessionsController)
@TestMixin([grails.test.mixin.support.GrailsUnitTestMixin, grails.test.mixin.web.FiltersUnitTestMixin])
class FederatedSessionsControllerSpec extends spock.lang.Specification {
  
  @Shared def shiroEnvironment = new ShiroEnvironment()
  
  def cleanupSpec() { 
    shiroEnvironment.tearDownShiro() 
  }

  def setup() {
    grailsApplication.config.aaf.base.realms.federated = [
      require: [
        sharedtoken: false,
        cn: false,
        email: false
      ]
    ]
  }

  def 'federatedincomplete'() {
    when:
    controller.federatedincomplete()
 
    then:
    true
  }
  
  def '403 when sp is disabled for federated login'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: false,
      development: [ active: false ],
    ] 
    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.addHeader("User-Agent", "Google Chrome X.Y")

    when:
    controller.federatedlogin()
    
    then:
    response.status == 403
    response.committed
  }
  
  def 'incomplete and redirect to federatedincomplete when principal not provided'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true ],
      
      mapping: [
        principal: 'persistent-id',   
        credential: 'Shib-Session-ID',
      ]
    ] 
    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.addHeader("User-Agent", "Google Chrome X.Y")

    when:
    controller.federatedlogin()
    
    then:
    view == "/federatedSessions/federatedincomplete"
    model.errors[0].contains("Your unique account identifier (persistent-id, urn:oid:1.3.6.1.4.1.5923.1.1.1.10) was unable to be obtained")
  }
  
  def 'incomplete and redirect to federatedincomplete when credential not provided'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id',   
        credential: 'Shib-Session-ID'
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.addHeader("User-Agent", "Google Chrome X.Y")

    when:
    controller.federatedlogin()
    
    then:
    view == "/federatedSessions/federatedincomplete"
    !model.errors[0].contains("Your unique account identifier (persistent-id, urn:oid:1.3.6.1.4.1.5923.1.1.1.10) was unable to be obtained")
    model.errors[0].contains("An internal SAML session identifier (Shib-Session-ID) was unable to be obtained from the provided assertion")
  }

  def 'incomplete and redirect to federatedincomplete when user agent not provided'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id',   
        credential: 'Shib-Session-ID'
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')

    when:
    controller.federatedlogin()
    
    then:
    view == "/federatedSessions/federatedincomplete"
    model.errors[0].contains("Browser User Agent was not presented")
  }

  def 'incomplete and redirect to federatedincomplete when shared token required but not provided'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ],

      require: [
        sharedtoken:true
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.addHeader("User-Agent", "Google Chrome X.Y")

    when:
    controller.federatedlogin()
    
    then:
    view == "/federatedSessions/federatedincomplete"
    model.errors[0].contains("Your identifier (auEduPersonSharedToken) was unable to be obtained from the provided assertion")
  }

  def 'incomplete and redirect to federatedincomplete when cn not provided'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ],

      require: [
        cn:true
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")

    when:
    controller.federatedlogin()
    
    then:
    view == "/federatedSessions/federatedincomplete"
    model.errors[0].contains("Your common name (cn, urn:oid:2.5.4.3) was unable to be obtained from the provided assertion")
  }

  def 'incomplete and redirect to federatedincomplete when email not provided'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ],

      require: [
        email:true
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Bradley Beddoes')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")

    when:
    controller.federatedlogin()
    
    then:
    view == "/federatedSessions/federatedincomplete"
    model.errors[0].contains("Your email address (mail, urn:oid:0.9.2342.19200300.100.1.3) was unable to be obtained from the provided assertion")
  }
  
  def 'redirect to root URI when all is valid and no target supplied'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def token
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( { t -> token = t; t instanceof FederatedToken } )
    token.principal == 'http://test.com!http://sp.test.com!1234'
    token.credential == '1234-mockid-5678'
    token.userAgent == "Google Chrome X.Y"
    response.redirectedUrl == '/'
  }

  def 'correctly trim UA when longer then 254 char'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Lorem ipsum dolor sit amet, nonummy ligula volutpat hac integer nonummy. Suspendisse ultricies, congue etiam tellus, erat libero, nulla eleifend, mauris pellentesque. Suspendisse integer praesent vel, integer gravida mauris, fringilla vehicula lacinia non")
    
    def token
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( { t -> token = t; t instanceof FederatedToken } )
    token.principal == 'http://test.com!http://sp.test.com!1234'
    token.credential == '1234-mockid-5678'
    token.userAgent == "Lorem ipsum dolor sit amet, nonummy ligula volutpat hac integer nonummy. Suspendisse ultricies, congue etiam tellus, erat libero, nulla eleifend, mauris pellentesque. Suspendisse integer praesent vel, integer gravida mauris, fringilla vehicula lacinia no"
    response.redirectedUrl == '/'
  }

  def 'when using headers redirect to root URI when all is valid and no target supplied'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: false],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.addHeader('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.addHeader('Shib-Session-ID', '1234-mockid-5678')
    request.addHeader('Shib-Identity-Provider', 'https://entity.com/id')
    request.addHeader('cn', 'Fred Bloggs')
    request.addHeader('mail', 'fred@uni.edu.au')
    request.addHeader('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def token
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( { t -> token = t; t instanceof FederatedToken } )
    token.principal == 'http://test.com!http://sp.test.com!1234'
    token.credential == '1234-mockid-5678'
    token.userAgent == "Google Chrome X.Y"
    response.redirectedUrl == '/'
  }

  def 'when using headers redirect to root URI when all is valid and but no shared token is supplied'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: false],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.addHeader('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.addHeader('Shib-Session-ID', '1234-mockid-5678')
    request.addHeader('Shib-Identity-Provider', 'https://entity.com/id')
    request.addHeader('auEduPersonSharedToken', '12345678')
    request.addHeader('cn', 'Fred Bloggs')
    request.addHeader('mail', 'fred@uni.edu.au')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def token
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( { t -> token = t; t instanceof FederatedToken } )
    token.principal == 'http://test.com!http://sp.test.com!1234'
    token.credential == '1234-mockid-5678'
    token.userAgent == "Google Chrome X.Y"
    response.redirectedUrl == '/'
  }
  
  def 'redirect to target URI when all is valid and target supplied'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def token
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    session[AuthController.TARGET] = '/some/test/content'
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( { t -> token = t; t instanceof FederatedToken } )
    token.principal == 'http://test.com!http://sp.test.com!1234'
    token.credential == '1234-mockid-5678'
    token.userAgent == "Google Chrome X.Y"
    response.redirectedUrl == '/some/test/content'
  }
  
  def 'redirect to federatederror when IncorrectCredentialsException thrown'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    session['grails.controllers.aaf.base.identity.AuthController:TARGETURI'] = '/some/test/content'
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( _ as FederatedToken ) >> { throw new IncorrectCredentialsException('test') }
    response.redirectedUrl == "/auth/federatederror"
  }
  
  def 'redirect to federatederror when DisabledAccountException thrown'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    session['grails.controllers.aaf.base.identity.AuthController:TARGETURI'] = '/some/test/content'
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( _ as FederatedToken ) >> { throw new DisabledAccountException('test') }
    response.redirectedUrl == "/auth/federatederror"
  }
  
  def 'redirect to federatederror when AuthenticationException thrown'() {
    setup:
    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: false ],
      
      request: [ attributes: true],
      
      mapping: [
        principal: 'persistent-id', 
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ] 

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    request.setAttribute('persistent-id', 'http://test.com!http://sp.test.com!1234')
    request.setAttribute('Shib-Session-ID', '1234-mockid-5678')
    request.setAttribute('Shib-Identity-Provider', 'https://entity.com/id')
    request.setAttribute('cn', 'Fred Bloggs')
    request.setAttribute('mail', 'fred@uni.edu.au')
    request.setAttribute('auEduPersonSharedToken', 'LGW3wpNaPgwnLoYYsghGbz1')
    request.addHeader("User-Agent", "Google Chrome X.Y")
    
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    
    session['grails.controllers.aaf.base.identity.AuthController:TARGETURI'] = '/some/test/content'
    
    when:
    controller.federatedlogin()
    
    then:
    1 * subject.login( _ as FederatedToken ) >> { throw new AuthenticationException('test') }
    response.redirectedUrl == "/auth/federatederror"
  }

  def 'accept injected attributes when in development mode'() {
    setup:
    defineBeans {
      developmentAttributesService(DevelopmentAttributesService) {
        it.autowire = 'byName'
      }
    }

    grailsApplication.config.grails.serverURL = 'localhost'
    request.addHeader('Referer', 'localhost')

    grailsApplication.config.aaf.base.realms.federated << [
      active: true,
      development: [ active: true ],

      request: [ attributes: true ],

      mapping: [
        principal: 'persistent-id',
        credential: 'Shib-Session-ID',
        entityID: 'Shib-Identity-Provider',
        cn: 'cn',
        email: 'mail',
        sharedToken: 'auEduPersonSharedToken',
      ]
    ]

    controller.metaClass.getGrailsApplication = { -> [config: Holders.config]}
    params.'attributes.persistent-id' = 'http://test.com!http://sp.test.com!1234'
    params.'attributes.Shib-Session-ID' = '1234-mockid-5678'
    params.'attributes.Shib-Identity-Provider' = 'https://entity.com/id'
    params.'attributes.cn' = 'Fred Bloggs'
    params.'attributes.mail' = 'fred-injected@uni.edu.au'
    params.'attributes.auEduPersonSharedToken' = 'LGW3wpNaPgwnLoYYsghGbz1'
    request.addHeader("User-Agent", "Google Chrome X.Y")
    request.method = 'POST'

    def token
    def model
    def subject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(subject)
    mockFilters(aaf.base.AAFBaseSecurityFilters)

    when:
    withFilters(controller: 'federatedSessions', action: 'federatedlogin') {
      model = controller.federatedlogin()
    }

    then:
    1 * subject.login( { t -> token = t; t instanceof FederatedToken } )
    token.principal == 'http://test.com!http://sp.test.com!1234'
    token.credential == '1234-mockid-5678'
    token.userAgent == "Google Chrome X.Y"
  }

}
