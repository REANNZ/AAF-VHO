package aaf.base

import aaf.base.admin.EmailTemplate

import org.springframework.web.context.request.RequestContextHolder
import org.codehaus.groovy.grails.web.context.ServletContextHolder
import org.springframework.web.context.support.WebApplicationContextUtils

class EmailManagerService {
  def mailService
  def groovyPagesTemplateEngine
  def groovyPageRenderer

  public void send(String _to, String _subject, EmailTemplate view, Map model, String _cc = null, String _bcc = null) {

    // groovyPagesTemplate requires request object
    // create one if we're calling from quartz etc which doesn't have a request bound
    def webRequest = RequestContextHolder.getRequestAttributes()
    if(!webRequest) {
      def servletContext  = ServletContextHolder.getServletContext()
      def applicationContext = WebApplicationContextUtils.getRequiredWebApplicationContext(servletContext)
      webRequest = grails.util.GrailsWebUtil.bindMockWebRequest(applicationContext)
    }

    def output = new StringWriter()
    groovyPagesTemplateEngine.createTemplate(view.content, view.name).make(model).writeTo(output)
    def contents = output.toString()

    def email_msg = groovyPageRenderer.render(view: '/layouts/email')
    email_msg = email_msg.replace("###HTML EMAIL CONTENT###", contents)
   
    mailService.sendMail {
      to _to  

      if(_cc)
        cc _cc
        
      if(_bcc)
        bcc _bcc

      subject _subject
      html email_msg
    }
  }

}
