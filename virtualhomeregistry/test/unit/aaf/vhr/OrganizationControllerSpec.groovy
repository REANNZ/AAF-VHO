package aaf.vhr

import grails.test.mixin.*
import grails.plugin.spock.*
import grails.buildtestdata.mixin.Build

import spock.lang.*

import test.shared.ShiroEnvironment

@TestFor(aaf.vhr.OrganizationController)
@Build([Organization])
class OrganizationControllerSpec  extends spock.lang.Specification {

  def subject
  @Shared def shiroEnvironment = new ShiroEnvironment()
  
  def cleanupSpec() { 
    shiroEnvironment.tearDownShiro() 
  }

  def setup() {
    subject = Mock(org.apache.shiro.subject.Subject)

    subject.isAuthenticated() >> true
    shiroEnvironment.setSubject(subject)
    
    controller.metaClass.getSubject = { [id:1, principal:'http://test.com!http://sp.test.com!1234'] }
  }

  def 'ensure beforeInterceptor only excludes list, create, save'() {
    when:
    controller

    then:
    controller.beforeInterceptor.except.size() == 3
    controller.beforeInterceptor.except.containsAll(['list', 'create', 'save'])
  }

  def 'ensure redirect to list if no ID presented to beforeInterceptor'() {
    when:
    def result = controller.validOrganization()

    then:
    !result
    response.status == 302

    response.redirectedUrl == "/organization/list"

    flash.type == 'info'
    flash.message == 'controllers.aaf.vhr.organization.no.id'
  }

  def 'ensure redirect to list if no valid instance found by beforeInterceptor'() {
    when:
    params.id = 1
    def result = controller.validOrganization()

    then:
    !result
    response.status == 302

    response.redirectedUrl== "/organization/list"

    flash.type == 'info'
    flash.message == 'controllers.aaf.vhr.organization.notfound'
  }

  def 'ensure correct output from list'() {
    setup:
    (1..10).each { Organization.build() }

    when:
    params.max = max
    def model = controller.list()

    then:
    Organization.count() == total
    model.organizationInstanceList.size() == expectedResult

    where:
    max | total | expectedResult
    0 | 10 | 10
    5 | 10 | 5
  }

  def 'ensure correct output from show'() {
    setup:
    def organizationTestInstance = Organization.build()

    when:
    params.id = organizationTestInstance.id
    def model = controller.show()

    then:
    model.organizationInstance == organizationTestInstance
  }

  def 'ensure correct output from create when valid permission'() {
    setup:
    subject.isPermitted("app:manage:organization:create") >> true

    when:
    def model = controller.create()

    then:
    model.organizationInstance.instanceOf(Organization)
  }

  def 'ensure correct output from create when invalid permission'() {
    setup:
    subject.isPermitted("app:manage:organization:create") >> false

    when:
    def model = controller.create()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from save when invalid permission'() {
    setup:
    subject.isPermitted("app:manage:organization:create") >> false

    when:
    def model = controller.save()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from save with invalid data and when valid permission'() {
    setup:
    subject.isPermitted("app:manage:organization:create") >> true

    def organizationTestInstance = Organization.build()
    organizationTestInstance.properties.each {
      if(it.value)
        params."${it.key}" = "${it.value}"
    }
    organizationTestInstance.delete()

    Organization.metaClass.save { null }
    
    when:
    controller.save()

    then:
    Organization.count() == 0
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.organization.save.failed'

    model.organizationInstance.properties.each {
      it.value == organizationTestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from save with valid data and when valid permission'() {
    setup:
    subject.isPermitted("app:manage:organization:create") >> true

    def organizationTestInstance = Organization.build()
    organizationTestInstance.properties.each {
      if(it.value)
        params."${it.key}" = "${it.value}"
    }
    organizationTestInstance.delete()

    expect:
    Organization.count() == 0

    when:
    controller.save()

    then:
    Organization.count() == 1
    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.organization.save.success'

    def savedOrganizationTestInstance = Organization.first()
    savedOrganizationTestInstance.properties.each {
      it.value == organizationTestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from edit when invalid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:edit") >> false

    when:
    params.id = organizationTestInstance.id
    def model = controller.edit()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from edit when valid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:edit") >> true

    when:
    params.id = organizationTestInstance.id
    def model = controller.edit()

    then:
    model.organizationInstance == organizationTestInstance
  }

  def 'ensure correct output from update when invalid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance}.id}:edit") >> false

    when:
    def model = controller.update()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from update with null version but valid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:edit") >> true
    
    expect:
    Organization.count() == 1

    when:
    params.id = organizationTestInstance.id
    params.version = null
    controller.update()

    then:
    Organization.count() == 1
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.organization.update.noversion'
  }

  def 'ensure correct output from update with invalid data and when valid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:edit") >> true
    organizationTestInstance.getVersion() >> 20
    
    organizationTestInstance.properties.each {
      if(it.value)
        params."${it.key}" = "${it.value}"
    }
    Organization.metaClass.save { null }
    
    expect:
    Organization.count() == 1

    when:
    params.id = organizationTestInstance.id
    params.version = 1
    controller.update()

    then:
    Organization.count() == 1
    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.organization.update.failed'

    model.organizationInstance.properties.each {
      it.value == organizationTestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from update with valid data and when valid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:edit") >> true
    
    organizationTestInstance.properties.each {
      if(it.value)
        params."${it.key}" = "${it.value}"
    }

    expect:
    Organization.count() == 1

    when:
    params.id = organizationTestInstance.id
    params.version = 0
    controller.update()

    then:
    Organization.count() == 1
    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.organization.update.success'

    def savedOrganizationTestInstance = Organization.first()
    savedOrganizationTestInstance == organizationTestInstance

    savedOrganizationTestInstance.properties.each {
      it.value == organizationTestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from delete when invalid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:delete") >> false

    when:
    params.id = organizationTestInstance.id
    def model = controller.delete()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from delete when valid permission'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:delete") >> true

    expect:
    Organization.count() == 1

    when:
    params.id = organizationTestInstance.id
    def model = controller.delete()

    then:
    Organization.count() == 0

    response.redirectedUrl == "/organization/list"

    flash.type == 'success'
    flash.message == 'controllers.aaf.vhr.organization.delete.success'
  }

  def 'ensure correct output from delete when integrity violation'() {
    setup:
    def organizationTestInstance = Organization.build()
    subject.isPermitted("app:manage:organization:${organizationTestInstance.id}:delete") >> true

    Organization.metaClass.delete { throw new org.springframework.dao.DataIntegrityViolationException("Thrown from test case") }

    expect:
    Organization.count() == 1

    when:
    params.id = organizationTestInstance.id
    def model = controller.delete()

    then:
    Organization.count() == 1

    response.redirectedUrl == "/organization/show/${organizationTestInstance.id}"

    flash.type == 'error'
    flash.message == 'controllers.aaf.vhr.organization.delete.failure'
  }
}