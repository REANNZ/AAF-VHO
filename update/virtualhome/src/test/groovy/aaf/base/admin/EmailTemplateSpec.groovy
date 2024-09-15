package aaf.base.admin

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class EmailTemplateSpec extends Specification implements DomainUnitTest<EmailTemplate> {

  def 'ensure name must not be null or blank'() {
    setup:
    def ev = new EmailTemplate(content:"test content")
    //mockForConstraintsTests(EmailTemplate, [ev])

    when:
    ev.name = val
    def result = ev.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == ev.errors['content']

    where:
    val << [null, '', "newsletter"]
    reason << ['nullable', 'blank', '']
    expectedResult << [false, false, true]
  }

  def 'ensure content must not be null or blank and contains HTML replacement banner'() {
    setup:
    def ev = new EmailTemplate(name:"test name")
    //mockForConstraintsTests(EmailTemplate, [ev])

    when:
    ev.content = val
    def result = ev.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == ev.errors['content']

    where:
    val << [null, '', "<div>content</div>"]
    reason << ['nullable', 'blank', '']
    expectedResult << [false, false, true]
  }

}
