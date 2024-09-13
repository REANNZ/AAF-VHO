package aaf.base.admin

import grails.test.mixin.*
import grails.buildtestdata.mixin.Build
import spock.lang.*
import grails.test.spock.*

@TestFor(aaf.base.admin.EmailTemplateController)
@Mock([EmailTemplate])
@Build([EmailTemplate])
class EmailTemplateControllerSpec extends spock.lang.Specification {

  def "ensure default action"() {
    expect:
    controller.defaultAction == "list"
  }

  def "ensure list of EmailTemplates returned"() {
    setup:
    (1..10).each { EmailTemplate.build() }

    when:
    def model = controller.list()

    then:
    EmailTemplate.count() == 10
    model.emailtemplateList != null
    model.emailtemplateList.size() == 10
  }

  def "ensure create seeds EmailTemplate object"() {
    when:
    def model = controller.create()

    then:
    EmailTemplate.count() == 0
    model.emailtemplate != null
    model.emailtemplate.class == EmailTemplate
  }

  def "ensure saves requires valid EmailTemplate"() {
    when:
    controller.save()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.save.failure'
    view == '/emailTemplate/create'
    model.emailtemplate != null
  }

  def "ensure save requires valid EmailTemplate name"() {
    setup:
    params.content = 'testing content'

    when:
    controller.save()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.save.failure'
    view == '/emailTemplate/create'
    model.emailtemplate != null
    model.emailtemplate.errors['name'] != 0
  }

  def "ensure save requires valid EmailTemplate content"() {
    setup:
    params.name = 'template_name'
    
    when:
    controller.save()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.save.failure'
    view == '/emailTemplate/create'
    model.emailtemplate != null
    model.emailtemplate.errors['content'] != 0
  }

  def "ensure save when valid EmailTemplate"() {
    setup:
    controller.metaClass.getSubject = { [id:1, principal:'http://test.com!http://sp.test.com!1234'] }

    params.name = 'template_name2'
    params.content = 'testing content'
    
    when:
    controller.save()

    then:
    flash.type == 'success'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.save.success'
    response.redirectedUrl == '/emailTemplate/show/1'
  }

  def "ensure show when valid EmailTemplate"() {
    setup:
    def et = EmailTemplate.build()
    params.id = et.id

    when:
    def model = controller.show()

    then:
    model.emailtemplate == et
  }

  def "ensure edit when valid EmailTemplate"() {
    setup:
    def et = EmailTemplate.build()
    params.id = et.id

    when:
    def model = controller.edit()

    then:
    model.emailtemplate == et
  }

  def "ensure update requires valid EmailTemplate name"() {
    setup:
    def et = EmailTemplate.build()
    params.id = et.id
    params.name = ''
    params.content = 'testing content'

    when:
    controller.update()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.update.failure'
    view == '/emailTemplate/edit'
    model.emailtemplate != null
    model.emailtemplate.errors['name'] != 0
  }

  def "ensure update requires valid EmailTemplate content"() {
    setup:
    def et = EmailTemplate.build()
    params.id = et.id
    params.name = 'template_name'
    params.content = ''
    
    when:
    controller.update()

    then:
    flash.type == 'error'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.update.failure'
    view == '/emailTemplate/edit'
    model.emailtemplate != null
    model.emailtemplate.errors['content'] != 0
  }

  def "ensure update when valid EmailTemplate"() {
    setup:
    def et = EmailTemplate.build()
    controller.metaClass.getSubject = { [id:1, principal:'http://test.com!http://sp.test.com!1234'] }

    params.id = et.id
    params.name = 'template_name2'
    params.content = 'testing content'
    
    when:
    controller.update()

    then:
    flash.type == 'success'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.update.success'
    response.redirectedUrl == "/emailTemplate/show/${et.id}"
  }

  def "validEmailTemplate rejects request when no id presented"() {
    when:
    def result = controller.validEmailTemplate()

    then:
    !result
    flash.type == 'info'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.noemailtemplateid'
    response.redirectedUrl == "/emailTemplate/list"
  }

  def "validEmailTemplate rejects request when invalid EmailTemplate requested"() {
    setup:
    params.id = 1000

    when:
    def result = controller.validEmailTemplate()

    then:
    !result
    flash.type == 'error'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.nonexistant'
    response.redirectedUrl == "/emailTemplate/list"
  }

  def "validEmailTemplate approves request when invalid EmailTemplate requested"() {
    setup:
    def et = EmailTemplate.build()
    params.id = et.id

    when:
    def result = controller.validEmailTemplate()

    then:
    result == null
  }

}
