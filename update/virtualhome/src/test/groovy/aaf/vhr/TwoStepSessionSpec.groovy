package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import aaf.vhr.ManagedSubject

import test.shared.ShiroEnvironment

import grails.testing.gorm.DomainUnitTest

class TwoStepSessionSpec extends Specification implements DomainUnitTest<TwoStepSession> {

  def 'populate setups up initial session state'() {
    setup:
    def c = new TwoStepSession()

    when:
    c.populate()

    then:
    c.expiry != null
    c.value != null
    c.value.length() == 64
  }

}
