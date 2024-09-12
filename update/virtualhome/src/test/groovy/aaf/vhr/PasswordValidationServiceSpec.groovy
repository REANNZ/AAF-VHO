package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import aaf.vhr.ManagedSubject

import grails.testing.services.ServiceUnitTest

class PasswordValidationServiceSpec extends Specification implements ServiceUnitTest<PasswordValidationService> {

  def pv
  def cs

  def setup() {
    cs = new CryptoService(grailsApplication: grailsApplication)
    grailsApplication.config.aaf.vhr.crypto.log_rounds = 4
    grailsApplication.config.aaf.vhr.crypto.sha_rounds = 2048
    pv = new PasswordValidationService(grailsApplication: grailsApplication, cryptoService: cs)
  }

  def 'confirm passwords submitted must match each other'() {
    setup:
    def subject = new ManagedSubject()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val + "abcdefg"

    when:
    def result = pv.validate(subject)

    then:
    !result[0]
    result[1][0] == 'aaf.vhr.passwordvalidationservice.notmatching'

    where:
      val << ['Ab1!deXgh', 'Ab1!deXghFBVGs183TrhiEsFtWCV3X4lp0og', 'abcd', 'a']
  }

  def 'confirm passwords submitted must not contain the login name'() {
    setup:
    def subject = new ManagedSubject()
    subject.login = 'harry'
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    !result[0]
    result[1][0] == 'aaf.vhr.passwordvalidationservice.containslogin'

    where:
      val << ['Ab1!deXgharry', 'Ab1!deXghFBVGs183TrhiEsFtWCV3X4lp0ogharry', 'abcdharry', 'aharry']
  }

  def 'confirm password submitted is not the same as previously used'() {
    setup:
    def subject = new ManagedSubject()
    subject.login = 'harry'
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val
    cs.generatePasswordHash(subject)

    when:
    def result = pv.validate(subject)

    then:
    !result[0]
    result[1][0] == 'aaf.vhr.passwordvalidationservice.reused'

    where:
      val << ['Ab1!deXg', 'Ab1!deXghFBVGs183TrhiEsFtWCV3X4lp0og']
  }

  def 'confirm password meets NIST minimum of 8 char for user generated pw'() {
    setup:
    def subject = new ManagedSubject()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.toshort')
    }

    where:
      val << ['Ab1!deXgh', 'Ab1!deXghFBVGs183TrhiEsFtWCV3X4lp0og', 'abcd', 'a', '']
      expected << [true, true, false, false, false]
  }

  def 'confirm short password meets char characteristic rules'() {
    setup:
    def subject = new ManagedSubject()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.insufficent.characteristics')
    }

    where:
      val << ['Abb!deXgh', 'Ab1ddeXgh', 'ab1!dexgh', 'AB1!DEXGH', 'Ab1!deXgh']
      expected << [false, true, false, false, true]
  }

  def 'confirm longer password (16+) is allowed dictionary words and does not need to meet char characteristics'() {
    setup:
    def subject = new ManagedSubject()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    where:
      val << ['thehorseZEBRAlikedIceland', 'icelandisagreatplacetovisit', 'ICELAND was a great experience!', 'pizzlepixythermosvenosity']
      expected << [true, true, true, true]
  }

}
