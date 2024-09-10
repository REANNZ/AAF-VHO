<%=packageName ? "package ${packageName}" : ''%>

import grails.test.mixin.*
import grails.test.spock.*
import grails.buildtestdata.mixin.Build

import spock.lang.*

import test.shared.ShiroEnvironment

@TestFor(${packageName}.${className}Controller)
@Build([${className}, aaf.base.identity.Subject])
class ${className}ControllerSpec  extends spock.lang.Specification {
  
  @Shared def shiroEnvironment = new ShiroEnvironment()

  aaf.base.identity.Subject subject
  org.apache.shiro.subject.Subject shiroSubject
  
  def cleanupSpec() { 
    shiroEnvironment.tearDownShiro() 
  }

  def setup() {
    subject = aaf.base.identity.Subject.build()

    shiroSubject = Mock(org.apache.shiro.subject.Subject)
    shiroSubject.id >> subject.id
    shiroSubject.principal >> subject.principal
    shiroSubject.isAuthenticated() >> true
    shiroEnvironment.setSubject(shiroSubject)
    
    controller.metaClass.getSubject = { subject }
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
    def result = controller.valid${className}()

    then:
    !result
    response.status == 302

    response.redirectedUrl == "/${propertyName}/list"

    flash.type == 'info'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.no.id'
  }

  def 'ensure redirect to list if no valid instance found by beforeInterceptor'() {
    when:
    params.id = 1
    def result = controller.valid${className}()

    then:
    !result
    response.status == 302

    response.redirectedUrl== "/${propertyName}/list"

    flash.type == 'info'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.notfound'
  }

  def 'ensure correct output from list'() {
    setup:
    (1..10).each { ${className}.build() }

    when:
    params.max = max
    def model = controller.list()

    then:
    ${className}.count() == total
    model.${propertyName}InstanceList.size() == expectedResult

    where:
    max | total | expectedResult
    0 | 10 | 10
    5 | 10 | 5
  }

  def 'ensure correct output from show'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()

    when:
    params.id = ${propertyName}TestInstance.id
    def model = controller.show()

    then:
    model.${propertyName}Instance == ${propertyName}TestInstance
  }

  def 'ensure correct output from create when valid permission'() {
    setup:
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:create") >> true

    when:
    def model = controller.create()

    then:
    model.${propertyName}Instance.instanceOf(${className})
  }

  def 'ensure correct output from create when invalid permission'() {
    setup:
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:create") >> false

    when:
    def model = controller.create()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from save when invalid permission'() {
    setup:
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:create") >> false

    when:
    def model = controller.save()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from save with invalid data and when valid permission'() {
    setup:
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:create") >> true

    def ${propertyName}TestInstance = ${className}.build()
    ${propertyName}TestInstance.properties.each {
      if(it.value) {
        if(grailsApplication.isDomainClass(it.value.getClass()))
          params."\${it.key}" = [id:"\${it.value.id}"]
        else
          params."\${it.key}" = "\${it.value}"
      }
    }
    ${propertyName}TestInstance.delete()

    ${className}.metaClass.save { null }
    
    when:
    controller.save()

    then:
    ${className}.count() == 0
    flash.type == 'error'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.save.failed'

    model.${propertyName}Instance.properties.each {
      it.value == ${propertyName}TestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from save with valid data and when valid permission'() {
    setup:
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:create") >> true

    def ${propertyName}TestInstance = ${className}.build()
    ${propertyName}TestInstance.properties.each {
      if(it.value) {
        if(grailsApplication.isDomainClass(it.value.getClass()))
          params."\${it.key}" = [id:"\${it.value.id}"]
        else
          params."\${it.key}" = "\${it.value}"
      }
    }
    ${propertyName}TestInstance.delete()

    expect:
    ${className}.count() == 0

    when:
    controller.save()

    then:
    ${className}.count() == 1
    flash.type == 'success'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.save.success'

    def saved${className}TestInstance = ${className}.first()
    saved${className}TestInstance.properties.each {
      it.value == ${propertyName}TestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from edit when invalid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:edit") >> false

    when:
    params.id = ${propertyName}TestInstance.id
    def model = controller.edit()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from edit when valid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:edit") >> true

    when:
    params.id = ${propertyName}TestInstance.id
    def model = controller.edit()

    then:
    model.${propertyName}Instance == ${propertyName}TestInstance
  }

  def 'ensure correct output from update when invalid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance}.id}:edit") >> false

    when:
    def model = controller.update()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from update with null version but valid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:edit") >> true
    
    expect:
    ${className}.count() == 1

    when:
    params.id = ${propertyName}TestInstance.id
    params.version = null
    controller.update()

    then:
    ${className}.count() == 1
    flash.type == 'error'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.noversion'
  }

  def 'ensure correct output from update with invalid data and when valid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:edit") >> true
    ${propertyName}TestInstance.getVersion() >> 20
    
    ${propertyName}TestInstance.properties.each {
      if(it.value) {
        if(grailsApplication.isDomainClass(it.value.getClass()))
          params."\${it.key}" = [id:"\${it.value.id}"]
        else
          params."\${it.key}" = "\${it.value}"
      }
    }
    ${className}.metaClass.save { null }
    
    expect:
    ${className}.count() == 1

    when:
    params.id = ${propertyName}TestInstance.id
    params.version = 1
    controller.update()

    then:
    ${className}.count() == 1
    flash.type == 'error'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.failed'

    model.${propertyName}Instance.properties.each {
      it.value == ${propertyName}TestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from update with valid data and when valid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:edit") >> true
    
    ${propertyName}TestInstance.properties.each {
      if(it.value) {
        if(grailsApplication.isDomainClass(it.value.getClass()))
          params."\${it.key}" = [id:"\${it.value.id}"]
        else
          params."\${it.key}" = "\${it.value}"
      }
    }

    expect:
    ${className}.count() == 1

    when:
    params.id = ${propertyName}TestInstance.id
    params.version = 0
    controller.update()

    then:
    ${className}.count() == 1
    flash.type == 'success'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.success'

    def saved${className}TestInstance = ${className}.first()
    saved${className}TestInstance == ${propertyName}TestInstance

    saved${className}TestInstance.properties.each {
      it.value == ${propertyName}TestInstance.getProperty(it.key)
    }
  }

  def 'ensure correct output from delete when invalid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:delete") >> false

    when:
    params.id = ${propertyName}TestInstance.id
    def model = controller.delete()

    then:
    model == null
    response.status == 403
  }

  def 'ensure correct output from delete when valid permission'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:delete") >> true

    expect:
    ${className}.count() == 1

    when:
    params.id = ${propertyName}TestInstance.id
    def model = controller.delete()

    then:
    ${className}.count() == 0

    response.redirectedUrl == "/${propertyName}/list"

    flash.type == 'success'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.delete.success'
  }

  def 'ensure correct output from delete when integrity violation'() {
    setup:
    def ${propertyName}TestInstance = ${className}.build()
    shiroSubject.isPermitted("app:manage:${propertyName.toLowerCase()}:\${${propertyName}TestInstance.id}:delete") >> true

    ${className}.metaClass.delete { throw new org.springframework.dao.DataIntegrityViolationException("Thrown from test case") }

    expect:
    ${className}.count() == 1

    when:
    params.id = ${propertyName}TestInstance.id
    def model = controller.delete()

    then:
    ${className}.count() == 1

    response.redirectedUrl == "/${propertyName}/show/\${${propertyName}TestInstance.id}"

    flash.type == 'error'
    flash.message == 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.delete.failure'
  }
}
