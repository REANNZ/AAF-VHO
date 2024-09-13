package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import aaf.vhr.ManagedSubject

import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class EmailResetSpec extends Specification implements DomainUnitTest<EmailReset>{

  def 'ensure code must not be null or blank and 24 characters long'() {
    setup:
    def er = new EmailReset()
    er.hash = 'x'.multiply(128)
    er.salt = 'x'.multiply(29)
    er.validUntil = Mock(Date)
    // er.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    er.subject = Mock(ManagedSubject)

    when:
    er.code = val
    def result = er.validate() 
    println er.errors

    then:
    result == expectedResult

    if (!expectedResult)
      reason == er.errors['code']

    where:
    val << [null, '',
            org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(23),
            org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(24),
            org.apache.commons.lang.RandomStringUtils.randomAlphanumeric(25)]
    expectedResult << [false,false,false,true,false]
    reason << ['nullable', 'blank', 'minSize', '', 'maxSize']
  }

  def 'ensure hash must not be null or blank and be at least 6 characters long'() {
    setup:
    def er = new EmailReset()
    er.code = 'x'.multiply(24)
    er.salt = 'x'.multiply(29)
    er.validUntil = Mock(Date)
    // er.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    er.subject = Mock(ManagedSubject)

    when:
    er.hash = val
    def result = er.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == er.errors['hash']

    where:
    val << [null, '', 
    '0e819f575d8ca7e9b12dec270db4208c0ae20746d647432b2f846aff7ffc559c1029b85b23b7d25fa42a4d39aa3f76f6f9199310472ab1cb28921e', 
    '0e819f575d8ca7e9b12dec270db4208c0ae20746d647432b2f846aff7ffc559c1029b85b23b7d25fa42a4d39aa3f76f6f9199310472ab1cb28921e3e5347db47', 
    '0e819f575d8ca7e9b12dec270db4208c0ae20746d647432b2f846aff7ffc559c1029b85b23b7d25fa42a4d39aa3f76f6f9199310472ab1cb28921e3e5347db477']
    reason << ['nullable', 'blank', 'minSize', '', 'maxSize']
    expectedResult << [false, false, false, true, false]
  }

  def 'ensure salt must not be null or blank and exactly 29 characters long'() {
    setup:
    def er = new EmailReset()
    er.code = 'x'.multiply(24)
    er.hash = 'x'.multiply(128)
    er.validUntil = Mock(Date)
    // er.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    er.subject = Mock(ManagedSubject)

    when:
    er.salt = val
    def result = er.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == er.errors['salt']

    where:
    val << [null, '', 
    '$2a$12$zJCuKWn8srzSFqCH8P', 
    '$2a$12$zJCuKWn8srzSFqCH8P/bAu',
    '$2a$12$zJCuKWn8srzSFqCH8P/bAu1']
    reason << ['nullable', 'blank', 'minSize', '', '']
    expectedResult << [false, false, false, true, false]
  }

  def 'ensure validUntil must not be null and a valid date'() {
    setup:
    def er = new EmailReset()
    er.code = 'x'.multiply(24)
    er.hash = 'x'.multiply(128)
    er.salt = 'x'.multiply(29)
    // er.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    er.subject = Mock(ManagedSubject)
    
    when:
    er.validUntil = val
    def result = er.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == er.errors['validUntil']

    where:
    val << [null, new Date()] 
    reason << ['nullable', '']
    expectedResult << [false, true]
  }
}
