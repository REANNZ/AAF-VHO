package aaf.vhr

import grails.test.mixin.*
import grails.buildtestdata.mixin.Build
import spock.lang.*
import grails.plugin.spock.*

import aaf.vhr.ManagedSubject

@TestFor(aaf.vhr.ManagedSubject)
@Build([ManagedSubject])
class PasswordValidationServiceSpec extends UnitSpec {

  def pv

  def setup() {
    pv = new PasswordValidationService(grailsApplication: grailsApplication)
  }

  def 'confirm passwords submitted must match each other'() {
    setup:
    def subject = ManagedSubject.build()
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
    def subject = ManagedSubject.build()
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

  def 'confirm password meets NIST minimum of 8 char for user generated pw'() {
    setup:
    def subject = ManagedSubject.build()
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

  def 'confirm password meets non whitespace rules'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.whitespace')
    }

    where:
      val << ['Ab1!deXgh', 'Ab1! deXgh', 'hello there cat tortoise man', 'hellotherecattortoiseman']
      expected << [true, false, false, true]
  }

  def 'confirm password meets alphabetical sequence rules'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.sequence')
    }

    where:
      val << ['Ab1!deXgh', 'Abc1!deXgh', 'Abd1!defgXgh', 'Abd1!DEFGXgh']
      expected << [true, false, false, false]
  }

  def 'confirm password meets numerical sequence rules'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.sequence')
    }

    where:
      val << ['Ab1!deXgh', 'Ab12!deXgh', 'Ab123!deXgh', 'Ab1!deXgh678']
      expected << [true, true, false, false]
  }

  def 'confirm password meets qwerty sequence rules'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.sequence')
    }

    where:
      val << ['Ab1!deXgh', 'Ab1!deXqW', 'Ab1!deXghqWe', 'Ab1!deXghxcVbNm']
      expected << [true, true, false, false]
  }

  def 'confirm password meets repeat character sequence rules'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.match')
    }

    where:
      val << ['Ab1!deXgh', 'Aaab1!deXgh', 'Aaaab1!deXgh', 'Ab1!deeeeeeXgh']
      expected << [true, true, false, false]
  }

  def 'confirm short password meets char characteristic rules'() {
    setup:
    def subject = ManagedSubject.build()
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
      expected << [false, false, false, false, true]
  }

  def 'confirm short password meets no dictionary word rules'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    if(!expected) {
      assert result[1].size() >= 1
      assert result[1].contains('aaf.vhr.passwordvalidationservice.dictionary.word')
    }

    where:
      val << ['Ab1!deXgh', 'Ab1!password', 'Abraham1!deXgh', 'Ab1!Xylic', 'Ab1!ZEBRA']
      expected << [true, false, false, false, false]
  }

  def 'confirm longer password (16+) is allowed dictionary words and does not need to meet char characteristics'() {
    setup:
    def subject = ManagedSubject.build()
    subject.plainPassword = val
    subject.plainPasswordConfirmation = val

    when:
    def result = pv.validate(subject)

    then:
    result[0] == expected

    where:
      val << ['thehorseZEBRAlikedIceland', 'icelandisagreatplacetovisit', 'ICELANDwasagreatexperience!', 'pizzlepixythermosvenosity']
      expected << [true, true, true, true]
  }

}
