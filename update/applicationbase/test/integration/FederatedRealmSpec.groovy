import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import org.apache.shiro.authc.*
import aaf.base.identity.*

class FederatedRealmSpec extends IntegrationSpec {

  def grailsApplication
  def roleService
  def permissionService

  def federatedRealm

  def setup() {
    federatedRealm = new FederatedRealm(grailsApplication:grailsApplication, roleService:roleService, permissionService: permissionService)
  }

  def 'ensure UnknownAccountException if realm is inactive'() {
    setup:
    def token = new FederatedToken()
    grailsApplication.config.aaf.base.realms.federated.active = false

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(UnknownAccountException)
    e.message == "Authentication attempt for federated provider, denying attempt as federation disabled"
  }

  def 'ensure UnknownAccountException if principal is null'() {
    setup:
    def token = new FederatedToken()
    grailsApplication.config.aaf.base.realms.federated.active = true

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(UnknownAccountException)
    e.message == "Authentication attempt for federated provider, denying attempt as no persistent identifier was provided"
  }

  def 'ensure UnknownAccountException if credential is null'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    grailsApplication.config.aaf.base.realms.federated.active = true

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(UnknownAccountException)
    e.message == "Authentication attempt for federated provider, denying attempt as no credential was provided"
  }

  def 'ensure UnknownAccountException if sharedToken is null'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.require.sharedtoken = true

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(UnknownAccountException)
    e.message == "Authentication attempt for federated provider, denying attempt as no shared token was provided"
  }

  def 'ensure DisabledAccountException if subject is unknown and auto provision is off'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = false
    token.attributes = [:]

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(DisabledAccountException)
    e.message == "Authentication attempt for federated provider, denying attempt as federation integration is denying automated account provisioning"
  }

  def 'ensure DisabledAccountException if subject is unknown and auto provision is off (sharedToken provided)'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = false
    token.attributes = [:]
    token.sharedToken = "12345678"

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(DisabledAccountException)
    e.message == "Authentication attempt for federated provider, denying attempt as federation integration is denying automated account provisioning"
  }

  def 'ensure RuntimeException if subject minimal data is not provided'() {
    setup:
    def token = new FederatedToken()
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.realms.federated.require.sharedtoken = true
    token.attributes = [:]

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(RuntimeException)
    e.message == "Authentication attempt for federated provider, denying attempt as no persistent identifier was provided"
  }

  def 'ensure Subject is created without any permission when provisioning enabled, data correct and initial_administrator_auto_populate is false'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = false
    token.attributes = [:]
    token.attributes.cn = "Test User"
    token.attributes.email = "test@user.com"
    token.sharedToken = "12345678"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    expect:
    aaf.base.identity.Subject.count() == 0
    Permission.count() == 0

    when:
    def account = federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.email == token.attributes.email
    subject.id == account.principals.asList()[0]
  }

  def 'ensure minimal Subject is created when provisioning enabled and minimal identity data only is provided (EPTID)'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = false
    token.attributes = [:]

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    expect:
    aaf.base.identity.Subject.count() == 0
    Permission.count() == 0

    when:
    def account = federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.cn == null
    subject.email == null
    subject.id == account.principals.asList()[0]
  }

  def 'ensure Subject is created without any sharedToken when provisioning enabled and all other data provided'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = false
    token.attributes = [:]
    token.attributes.cn = "Test User"
    token.attributes.email = "test@user.com"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    expect:
    aaf.base.identity.Subject.count() == 0
    Permission.count() == 0

    when:
    def account = federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.email == token.attributes.email
    subject.id == account.principals.asList()[0]
  }

  def 'ensure Subject is created with first email address when provisioning enabled and all other data provided'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = false
    token.attributes = [:]
    token.attributes.cn = "Test User"
    token.attributes.email = "test@user.com;test@internal.user.com"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    expect:
    aaf.base.identity.Subject.count() == 0
    Permission.count() == 0

    when:
    def account = federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.email == "test@user.com"
    subject.id == account.principals.asList()[0]
  }

  def 'ensure Subject is created without global permission when provisioning enabled, data correct, auto_populate is true and Subjects already exist'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test User"
    token.attributes.email = "test@user.com"
    token.sharedToken = "12345678"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!0987654321'
    subject2.cn = 'Second TestUser'
    subject2.email = 'test2@user.com'
    subject2.sharedToken = "87654321"
    subject2.save()

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    when:
    def account = federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 2
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.get(account.principals.asList()[0])
    subject.principal == token.principal
    subject.email == token.attributes.email

    subject.permissions == null
  }

  def 'ensure Subject located by EPTID and updates cn, email'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.enabled = true
    subject2.sharedToken='12345678'
    subject2.save()

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.cn == 'Test NewSurname'
    subject.email == 'new_email@user.com'

    subject.permissions == null
  }

  def 'ensure Subject located by EPTID with multiple email addresses updates cn, email'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com;new_email@internal.user.com"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.enabled = true
    subject2.sharedToken='12345678'
    subject2.save()

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.cn == 'Test NewSurname'
    subject.email == 'new_email@user.com'

    subject.permissions == null
  }

  def 'ensure Subject located by shared token and updates cn, email'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com"
    token.sharedToken = '12345678'

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!123'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.sharedToken = '12345678'
    subject2.enabled = true
    subject2.save()

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0

    def subject = aaf.base.identity.Subject.first()
    subject.principal == token.principal
    subject.cn == 'Test NewSurname'
    subject.email == 'new_email@user.com'

    subject.permissions == null
  }

  def 'ensure existing subject changing shared token throws exception'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com"
    token.sharedToken = '12345678'

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.sharedToken = '87654321'
    subject2.enabled = true
    subject2.save()

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(IncorrectCredentialsException)
    e.message == "${subject2} authentication halted as current sharedToken ${subject2.sharedToken} does not match incoming token ${token.sharedToken}"
  }

  def 'ensure existing subject failing to save throws exception'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com"
    token.sharedToken = '12345678'

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.sharedToken = '12345678'
    subject2.enabled = true
    subject2.save()

    subject2.metaClass.save = { null }

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(RuntimeException)
    e.message == "Account update exception for existing federated account ${token.principal}"
  }

  def 'ensure disabled subject fails authentication'() {
    setup:
    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com"
    token.sharedToken = '12345678'

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!123'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.sharedToken = '12345678'
    subject2.enabled = false
    subject2.save()

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(DisabledAccountException)
    e.message == "Attempt to authenticate using using federated principal mapped to a locally disabled account [${subject2.id}]${subject2.principal}"
  }

  def 'ensure Subject that fails session record save fails authentication'() {
    setup:
    def savedUpdate = false

    def token = new FederatedToken()
    token.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    token.credential='1234'
    token.sharedToken='12345678'
    grailsApplication.config.aaf.base.realms.federated.active = true
    grailsApplication.config.aaf.base.realms.federated.auto_provision = true
    grailsApplication.config.aaf.base.administration.initial_administrator_auto_populate = true
    token.attributes = [:]
    token.attributes.cn = "Test NewSurname"
    token.attributes.email = "new_email@user.com;new_email@internal.user.com"

    token.remoteHost="127.0.0.1"
    token.userAgent="Spock Browser"

    def subject2 = new aaf.base.identity.Subject()
    subject2.principal = 'https://idp.test.com/idp/shibboleth!https://sp.test.com/shibboleth!1234567890'
    subject2.cn = 'Test User'
    subject2.email = 'test@user.com'
    subject2.sharedToken='12345678'
    subject2.enabled = true
    subject2.save()

    subject2.metaClass.save = { if(!savedUpdate) { savedUpdate = true; true} else null }

    expect:
    aaf.base.identity.Subject.count() == 1
    Permission.count() == 0
    aaf.base.identity.Subject.first().cn == 'Test User'
    aaf.base.identity.Subject.first().email == 'test@user.com'

    when:
    federatedRealm.authenticate(token)

    then:
    def e = thrown(RuntimeException)
    e.message == "Account modification failed for ${token.principal} when adding new session record"
  }

}
