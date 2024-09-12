package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import aaf.vhr.ManagedSubject
import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class StateChangeSpec extends Specification implements DomainUnitTest<StateChange> {

  def 'ensure creation of basic state active change'() {
    setup:
    def managedSubjectTestInstance = new ManagedSubject()
    def change = new StateChange(event: StateChangeType.DEACTIVATE, reason:'system deactivated account')

    when:
    managedSubjectTestInstance.addToStateChanges(change)
    managedSubjectTestInstance.save()

    then:
    StateChange.count() == 1
    change.subject == managedSubjectTestInstance
    change.actionedBy == null
    managedSubjectTestInstance.stateChanges.size() == 1
  }

  def 'ensure creation of basic state locked change'() {
    setup:
    def managedSubjectTestInstance = new ManagedSubject()
    def change = new StateChange(event: StateChangeType.LOCKED, reason:'system locked account')

    when:
    managedSubjectTestInstance.addToStateChanges(change)
    managedSubjectTestInstance.save()

    then:
    StateChange.count() == 1
    change.subject == managedSubjectTestInstance
    change.actionedBy == null
    managedSubjectTestInstance.stateChanges.size() == 1
  }

  def 'ensure creation of basic state active change by administrator'() {
    setup:
    def administrator = new aaf.base.identity.Subject()
    def managedSubjectTestInstance = new ManagedSubject()
    def change = new StateChange(event: StateChangeType.DEACTIVATE, reason:'admin deactivated account', actionedBy:administrator)

    when:
    managedSubjectTestInstance.addToStateChanges(change)
    managedSubjectTestInstance.save()

    then:
    StateChange.count() == 1
    change.subject == managedSubjectTestInstance
    managedSubjectTestInstance.stateChanges.size() == 1
    managedSubjectTestInstance.stateChanges.toArray()[0].actionedBy == administrator
  }

  def 'ensure creation of basic state locked change by administrator'() {
    setup:
    def administrator = new aaf.base.identity.Subject()
    def managedSubjectTestInstance = new ManagedSubject()
    def change = new StateChange(event: StateChangeType.LOCKED, reason:'admin locked account', actionedBy:administrator)

    when:
    managedSubjectTestInstance.addToStateChanges(change)
    managedSubjectTestInstance.save()

    then:
    StateChange.count() == 1
    change.subject == managedSubjectTestInstance
    managedSubjectTestInstance.stateChanges.size() == 1
    managedSubjectTestInstance.stateChanges.toArray()[0].actionedBy == administrator
  }

  def 'ensure creation of extended state active change by administrator'() {
    setup:
    def administrator = new aaf.base.identity.Subject()
    def managedSubjectTestInstance = new ManagedSubject()
    def change = new StateChange(event: StateChangeType.DEACTIVATE, reason:'admin deactivated account', actionedBy:administrator)
    change.category = 'failed_lost_password'
    change.environment = """IP: 1.2.3.4
    Hostname: CPE-121-222-.lnse2.woo.bigpond.com
    Browser: Google Chrome 24.0"""

    when:
    managedSubjectTestInstance.addToStateChanges(change)
    managedSubjectTestInstance.save()

    then:
    StateChange.count() == 1
    change.subject == managedSubjectTestInstance
    managedSubjectTestInstance.stateChanges.size() == 1
    managedSubjectTestInstance.stateChanges.toArray()[0].actionedBy == administrator
    change.environment.contains ("woo.bigpond")
  }

}
