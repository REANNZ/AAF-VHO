package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import aaf.vhr.ManagedSubject
import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class ChallengeResponseSpec extends Specification implements DomainUnitTest<ChallengeResponse> {

  def 'ensure challenge must not be null or blank and be at least 6 characters long'() {
    setup:
    def cr = new ChallengeResponse()
    // cr.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    cr.subject = Mock(ManagedSubject)

    cr.hash = 'a'.multiply(128)
    cr.salt = 'b'.multiply(29)

    when:
    cr.challenge = val
    def result = cr.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == cr.errors['challenge']

    where:
    val << [null, '', '12345', '123456']
    reason << ['nullable', 'blank', 'minSize', '']
    expectedResult << [false, false, false, true]
  }

  def 'ensure hash must not be null or blank and be at least 6 characters long'() {
    setup:
    def cr = new ChallengeResponse()
    cr.challenge = '123456'
    cr.salt = 'b'.multiply(29)
    // cr.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    cr.subject = Mock(ManagedSubject)

    when:
    cr.hash = val
    def result = cr.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == cr.errors['hash']

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
    def cr = new ChallengeResponse()
    cr.hash = '0e819f575d8ca7e9b12dec270db4208c0ae20746d647432b2f846aff7ffc559c1029b85b23b7d25fa42a4d39aa3f76f6f9199310472ab1cb28921e3e5347db47'
    cr.challenge = '123456'
    // cr.subject cannot be null due to the belongsTo relationship, so we create a mock of it here
    cr.subject = Mock(ManagedSubject)

    when:
    cr.salt = val
    def result = cr.validate()

    then:
    result == expectedResult

    if (!expectedResult)
      reason == cr.errors['salt']

    where:
    val << [null, '', 
    '$2a$12$zJCuKWn8srzSFqCH8P', 
    '$2a$12$zJCuKWn8srzSFqCH8P/bAu',
    '$2a$12$zJCuKWn8srzSFqCH8P/bAu1']
    reason << ['nullable', 'blank', 'minSize', '', '']
    expectedResult << [false, false, false, true, false]
  }

}
