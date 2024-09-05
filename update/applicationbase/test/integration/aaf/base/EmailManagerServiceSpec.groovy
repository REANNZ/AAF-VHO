package aaf.base

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*
import com.icegreen.greenmail.util.*

import aaf.base.admin.EmailTemplate

class EmailManagerServiceSpec extends IntegrationSpec {

  def emailManagerService
  def greenMail

  def cleanup() {
    greenMail.deleteAllMessages()
  }

  def 'verify successful sending of basic email message'() {
    setup:
    def ev = new EmailTemplate(name:"testemail1", content: "TODAY123")

    when:
    emailManagerService.send('testuser@testdomain.com', 'email subject', ev, [:], )

    then:
    greenMail.getReceivedMessages().length == 1
    def message = greenMail.getReceivedMessages()[0]
    message.subject == 'email subject'
    message.to == 'testuser@testdomain.com'

    def expectedOutput = new File('test/data/email_basic.txt').text
    def messageContent = GreenMailUtil.getBody(message).toString()

    expectedOutput == messageContent
  }

  def 'verify successful sending of basic email message with CC'() {
    setup:
    def ev = new EmailTemplate(name:"testemail2", content: "TODAY123")

    when:
    emailManagerService.send('testuser@testdomain.com', 'email subject', ev, [:], 'cc@testdomain.com', 'bcc@testdomain.com')

    then:
    greenMail.getReceivedMessages().length == 3
    def message = greenMail.getReceivedMessages()[0]
    message.subject == 'email subject'
    message.to == 'testuser@testdomain.com'
    message.cc == 'cc@testdomain.com'

    def expectedOutput = new File('test/data/email_basic.txt').text
    def messageContent = GreenMailUtil.getBody(message).toString()

    expectedOutput == messageContent
  }

  def 'verify successful sending of complex email message using GSP taglibs and groovy code'() {
    setup:
    def ev = new EmailTemplate(name:"testemail3", content:new File('test/data/email_complex_template.gsp').text)

    when:
    emailManagerService.send('testuser@testdomain.com', 'complex email subject', ev, [subject:'test user', animals:['dogs', 'cats']])

    then:
    greenMail.getReceivedMessages().length == 1
    def message = greenMail.getReceivedMessages()[0]
    message.subject == 'complex email subject'
    message.to == 'testuser@testdomain.com'

    def expectedOutput = new File('test/data/email_complex.txt').text
    def messageContent = GreenMailUtil.getBody(message).toString()

    expectedOutput == messageContent
  }

}
