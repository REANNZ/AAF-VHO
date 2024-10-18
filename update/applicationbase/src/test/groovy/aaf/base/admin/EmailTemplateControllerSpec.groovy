package aaf.base.admin

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*
import grails.testing.web.controllers.ControllerUnitTest
import grails.testing.gorm.DomainUnitTest

class EmailTemplateControllerSpec extends Specification implements ControllerUnitTest<EmailTemplateController>, DomainUnitTest<EmailTemplate> {

  def "ensure default action"() {
    expect:
    controller.defaultAction == "list"
  }

  def "ensure list of EmailTemplates returned"() {
    setup:
    for (def i = 0; i < 10; i++) {
      new EmailTemplate(id: i, name: "Name ${i}", content: 'Content').save(flush:true, failOnError: true)
    }

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
    def et = new EmailTemplate(id: 1, name: 'Name', content: 'Content').save(flush:true, failOnError: true)
    params.id = et.id
    params.id = et.id

    when:
    def model = controller.show()

    then:
    model.emailtemplate == et
  }

  def "ensure edit when valid EmailTemplate"() {
    setup:
    def et = new EmailTemplate(id: 1, name: 'Name', content: 'Content').save(flush:true, failOnError: true)
    params.id = et.id

    when:
    def model = controller.edit()

    then:
    model.emailtemplate == et
  }

  def "ensure update requires valid EmailTemplate name"() {
    setup:
    def et = new EmailTemplate(id: 1, name: '', content: 'testing content').save(flush:true, validate: false)
    params.id = et.id
    params.name = et.name
    params.content = et.content

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
    def et = new EmailTemplate(id: 1, name: 'Name', content: '').save(flush:true, validate:false)
    params.id = et.id
    params.name = et.name
    params.content = et.content
    
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
    def et = new EmailTemplate(id: 1, name: 'template_name2', content: 'testing content').save(flush:true, failOnError: true)
    controller.metaClass.getSubject = { [id:1, principal:'http://test.com!http://sp.test.com!1234'] }

    params.id = et.id
    params.name = et.name
    params.content = et.content
    
    when:
    controller.update()

    then:
    flash.type == 'success'
    flash.message == 'controllers.aaf.base.admin.emailtemplate.update.success'
    response.redirectedUrl == "/emailTemplate/show/${et.id}"
  }
}
