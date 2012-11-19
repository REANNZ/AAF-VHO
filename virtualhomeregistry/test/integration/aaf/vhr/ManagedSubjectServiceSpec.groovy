package aaf.vhr

import grails.test.mixin.*
import grails.buildtestdata.mixin.Build
import spock.lang.*
import grails.plugin.spock.*
import com.icegreen.greenmail.util.*

import aaf.base.admin.EmailTemplate
import javax.mail.Message

class ManagedSubjectServiceSpec extends IntegrationSpec {
  
  def managedSubjectService
  def greenMail
  def cryptoService

  @Shared def grailsApplication
  @Shared def subject
  @Shared def role

  def setupSpec() {
    role = new aaf.base.identity.Role(name:'allsubjects')
    subject = new aaf.base.identity.Subject(principal:'http://idp.test.com/entity!http://sp.test.com/entity!1234', cn:'test subject', email:'testsubject@test.com', sharedToken:'1234sharedtoken')
    subject.save()
    subject.errors.each { println it }
    
    assert !subject.hasErrors()

    role.addToSubjects(subject)
    role.save()
    role.errors.each { println it }

    assert !role.hasErrors()

    SpecHelpers.setupShiroEnv(subject)

    grailsApplication.config.aaf.vhr.sharedtoken.idp_entityid="https://test.server.com/idp"
    grailsApplication.config.aaf.vhr.sharedtoken.sha_rounds=2048
    grailsApplication.config.aaf.vhr.crypto.log_rounds = 12
    grailsApplication.config.aaf.vhr.crypto.sha_rounds = 2048
  }

  def cleanup() {
    greenMail.deleteAllMessages()
  }

  def 'ensure successful finalize for ManagedSubject'() {
    setup:
    def o = Organization.build()
    def g = Group.build(organization: o)
    def ms = ManagedSubject.build(organization:o, group:g)
    def inv = ManagedSubjectInvitation.build(managedSubject: ms)

    expect:
    ManagedSubject.count() == 1
    ManagedSubjectInvitation.count() == 1
    o.subjects.size() == 1
    g.subjects.size() == 1

    when:
    def (result, managedSubject) = managedSubjectService.finalize(inv, 'usert', 'thisisalongpasswordtotest', 'thisisalongpasswordtotest')
    def invitation = ManagedSubjectInvitation.get(inv.id)

    then:
    result
    invitation.utilized
    managedSubject != null
    managedSubject.hasErrors() == false
    managedSubject.login == 'usert'
    cryptoService.verifyPasswordHash('thisisalongpasswordtotest', managedSubject)
  }

  def 'ensure failed finalize for ManagedSubject with poor password'() {
    setup:
    def o = Organization.build()
    def g = Group.build(organization: o)
    def ms = ManagedSubject.build(organization:o, group:g)
    def inv = new ManagedSubjectInvitation(managedSubject: ms).save()

    expect:
    ManagedSubject.count() == 1
    ManagedSubjectInvitation.count() == 1
    o.subjects.size() == 1
    g.subjects.size() == 1

    when:
    def (result, managedSubject) = managedSubjectService.finalize(inv, 'usert', 'insecurepw', 'insecurepw')
    inv.refresh()

    then:
    !result
    !inv.utilized
    managedSubject != null
    managedSubject.hasErrors()
    managedSubject.login == 'usert'
    managedSubject.hash == null
  }

  def 'ensure failed finalize for ManagedSubject with non matching password'() {
    setup:
    def o = Organization.build()
    def g = Group.build(organization: o)
    def ms = ManagedSubject.build(organization:o, group:g)
    def inv = ManagedSubjectInvitation.build(managedSubject: ms)

    expect:
    ManagedSubject.count() == 1
    ManagedSubjectInvitation.count() == 1
    o.subjects.size() == 1
    g.subjects.size() == 1

    when:
    def (result, managedSubject) = managedSubjectService.finalize(inv, 'usert', 'inzecurepW1!', 'inzecurepW1')
    inv.refresh()

    then:
    !result
    !inv.utilized
    managedSubject != null
    managedSubject.hasErrors()
    managedSubject.login == 'usert'
    managedSubject.hash == null
  }

  def 'ensure register creates new ManagedSubject'() {
    setup:
    def o = Organization.build()
    def g = Group.build(organization: o)
    def a = new Attribute(name:"eduPersonAffiliation", oid:"1.3.6.1.4.1.5923.1.1.1.1", description:"Specifies the persons relationship(s) to the institution").save()
    def et = new EmailTemplate(name:'registered_managed_subject', content: 'This is an email for ${managedSubject.cn} telling them to come and complete registration with code ${invitation.inviteCode}').save()
    
    expect:
    ManagedSubject.count() == 0
    Attribute.count() == 1
    o.subjects == null
    g.subjects == null

    when:
    def managedSubject = managedSubjectService.register('Test User', 'testuser@testdomain.com', 'student', o, g)
    o.refresh()
    g.refresh()

    then:
    managedSubject != null

    ManagedSubject.count() == 1
    managedSubject.cn == "Test User"
    managedSubject.email == "testuser@testdomain.com"
    !managedSubject.active
    managedSubject.pii[0].attribute == a
    managedSubject.pii[0].value == "student"

    greenMail.getReceivedMessages().length == 1

    def message = greenMail.getReceivedMessages()[0]
    message.subject == 'Action Required: Your new AAF VHR account is almost ready!'
    GreenMailUtil.getBody(message).contains('This is an email for Test User telling them')
    GreenMailUtil.getAddressList(message.getRecipients(Message.RecipientType.TO)) == 'testuser@testdomain.com'

    o.subjects.size() == 1
    g.subjects.size() == 1

    managedSubject.organization == o
    managedSubject.group == g
  }

  def 'ensure invalid CSV lines are rejected correctly'() {
    setup:
    def o = Organization.build()
    def g = Group.build(organization: o)

    expect:
    ManagedSubject.count() == 0

    when:
    def (result, errors, subjects, linesProcessed) = managedSubjectService.registerFromCSV(o, g, csv.bytes)

    then:
    !result
    errors.size() == expectedErrorCount
    linesProcessed == expectedLinesProcessed
    subjects == null

    ManagedSubject.count() == 0

    where:
    expectedErrorCount | expectedLinesProcessed | csv
    1 | 2 | "Test User,testuser@testdomain.com,staff,rubbish\nTest User,testuser@testdomain.com,staff"
    1 | 2 | "Mr Test User,testuser@testdomain.com,staff\nTest User,testuser@testdomain.com,staff"
    2 | 3 | "Test J User,testuser@testdomain.com,staff\n Mr Test User,testuser@testdomain.com,staff\nTest User,testuser@testdomain.com,staff"
    1 | 3 | "Test,testuser@testdomain.com,staff\n Mr Test User,testuser@testdomain.com,staff\nTest User,testuser@testdomain.com,staff"
  }

  def 'ensure valid CSV creates new ManagedSubject from each line'() {
    setup:
    def o = Organization.build()
    def g = Group.build(organization: o)
    def a = new Attribute(name:"eduPersonAffiliation", oid:"1.3.6.1.4.1.5923.1.1.1.1", description:"Specifies the persons relationship(s) to the institution").save()
    def et = new EmailTemplate(name:'registered_managed_subject', content: 'This is an email for ${managedSubject.cn} telling them to come and complete registration with code ${invitation.inviteCode}').save()
    
    expect:
    ManagedSubject.count() == 0
    Attribute.count() == 1
    o.subjects == null
    g.subjects == null

    when:
    def (result, errors, subjects, linesProcessed) = managedSubjectService.registerFromCSV(o, g, csv.bytes)
    o.refresh()
    g.refresh()

    then:
    result
    errors.size() == expectedErrorCount
    linesProcessed == expectedLinesProcessed
    subjects.size() == expectedLinesProcessed

    ManagedSubject.count() == expectedLinesProcessed
    subjects[0].cn == "Test User"
    subjects[0].email == "testuser@testdomain.com"
    !subjects[0].active
    subjects[0].pii[0].attribute == a
    subjects[0].pii[0].value == "student"
    subjects[0].organization == o
    subjects[0].group == g

    subjects[1].cn == "Test User2"
    subjects[1].email == "testuser2@testdomain.com"
    !subjects[1].active
    subjects[1].pii[0].attribute == a
    subjects[1].pii[0].value == "staff"
    subjects[1].organization == o
    subjects[1].group == g

    greenMail.getReceivedMessages().length == expectedLinesProcessed

    def message = greenMail.getReceivedMessages()[0]
    message.subject == 'Action Required: Your new AAF VHR account is almost ready!'
    GreenMailUtil.getBody(message).contains('This is an email for Test User telling them')
    GreenMailUtil.getAddressList(message.getRecipients(Message.RecipientType.TO)) == 'testuser@testdomain.com'

    def message2 = greenMail.getReceivedMessages()[1]
    message2.subject == 'Action Required: Your new AAF VHR account is almost ready!'
    GreenMailUtil.getBody(message2).contains('This is an email for Test User2 telling them')
    GreenMailUtil.getAddressList(message2.getRecipients(Message.RecipientType.TO)) == 'testuser2@testdomain.com'


    where:
    expectedErrorCount | expectedLinesProcessed | csv
    0 | 2 | "Test User,testuser@testdomain.com,student\nTest User2,testuser2@testdomain.com,staff"
  }

}
