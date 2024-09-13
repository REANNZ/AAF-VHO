package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import test.shared.ShiroEnvironment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class OrganizationSpec extends Specification implements DomainUnitTest<Organization> {

  @Shared def shiroEnvironment = new ShiroEnvironment()

  org.apache.shiro.subject.Subject shiroSubject
  
  def cleanupSpec() { 
    shiroEnvironment.tearDownShiro() 
  }

  def setup() {
    shiroSubject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(shiroSubject)
  }

  def 'ensure name can not be null or blank'() {
    setup:
    def o = new Organization()
    o.name = val
    o.displayName = 'dummy'

    when:
    def result = o.validate()

    then:
    result == expected

    if(!expected)
      reason == o.errors['name']

    where:
    val << [null, '', 'name']
    expected << [false, false, true]
    reason << ['nullable', 'blank', '']
  }

  def 'ensure displayName can not be null or blank'() {
    setup:
    def o = new Organization()
    o.name = 'dummy'
    o.displayName = val

    when:
    def result = o.validate()

    then:
    result == expected

    if(!expected)
      reason == o.errors['displayName']

    where:
    val << [null, '', 'name']
    expected << [false, false, true]
    reason << ['nullable', 'blank', '']
  }

  def 'ensure description can be null but not blank'() {
    setup:
    def o = new Organization()
    o.name = 'dummy'
    o.displayName = 'dummy'
    o.description = val

    when:
    def result = o.validate()

    then:
    result == expected

    if(!expected)
      reason == o.errors['description']

    where:
    val << [null, '', 'name']
    expected << [true, false, true]
    reason << ['', 'blank', '']
  }

  def 'Ensure unlimited and active org can register subjects'() {
    setup:
    def o = new Organization()
    o.active = true
    o.subjectLimit = 0

    when:
    def result = o.canRegisterSubjects()

    then:
    result
  }

  def 'Ensure unlimited but inactive org can register subjects'() {
    setup:
    def o = new Organization()
    o.active = false
    o.subjectLimit = 0

    when:
    def result = o.canRegisterSubjects()

    then:
    !result
  }

  def 'Ensure limited, active org that hasnt reached max can register subjects'() {
    setup:
    def o = new Organization()
    o.active = true
    o.subjectLimit = 100

    (1..99).each {
      def s = new ManagedSubject()
      o.addToSubjects(s)
    }

    when:
    def result = o.canRegisterSubjects()

    then:
    result
  }

  def 'Ensure limited, active org that has reached max cant register subjects'() {
    setup:
    def o = new Organization()
    o.active = true
    o.subjectLimit = 100

    (1..100).each {
      def s = new ManagedSubject()
      o.addToSubjects(s)
    }

    when:
    def result = o.canRegisterSubjects()

    then:
    !result
  }

  def 'Ensure limited, active org some how over max cant register subjects'() {
    setup:
    def o = new Organization()
    o.active = true
    o.subjectLimit = 100

    (1..101).each {
      def s = new ManagedSubject()
      o.addToSubjects(s)
    }

    when:
    def result = o.canRegisterSubjects()

    then:
    !result
  }

  def 'Ensure limited, inactive org that hasnt reached max cant register subjects'() {
    setup:
    def o = new Organization()
    o.active = false
    o.subjectLimit = 100

    (1..99).each {
      def s = new ManagedSubject()
      o.addToSubjects(s)
    }

    when:
    def result = o.canRegisterSubjects()

    then:
    !result
  }

  def 'Ensure limited, inactive org that has reached max cant register subjects'() {
    setup:
    def o = new Organization()
    o.active = false
    o.subjectLimit = 100

    (1..100).each {
      def s = new ManagedSubject()
      o.addToSubjects(s)
    }

    when:
    def result = o.canRegisterSubjects()

    then:
    !result
  }

  def 'Ensure limited, inactive org some how over max cant register subjects'() {
    setup:
    def o = new Organization()
    o.active = false
    o.subjectLimit = 100

    (1..101).each {
      def s = new ManagedSubject()
      o.addToSubjects(s)
    }

    when:
    def result = o.canRegisterSubjects()

    then:
    !result
  }

  def 'Ensure unlimited and active org can register groups'() {
    setup:
    def o = new Organization()
    o.active = true
    o.groupLimit = 0

    when:
    def result = o.canRegisterGroups()

    then:
    result
  }

  def 'Ensure unlimited but inactive org cant register groups'() {
    setup:
    def o = new Organization()
    o.active = false
    o.groupLimit = 0

    when:
    def result = o.canRegisterGroups()

    then:
    !result
  }

  def 'Ensure limited, active org that hasnt reached max can register groups'() {
    setup:
    def o = new Organization()
    o.active = true
    o.groupLimit = 100

    (1..99).each {
      def s = new Group()
      o.addToGroups(s)
    }

    when:
    def result = o.canRegisterGroups()

    then:
    result
  }

  def 'Ensure limited, active org that has reached max cant register groups'() {
    setup:
    def o = new Organization()
    o.active = true
    o.groupLimit = 100

    (1..100).each {
      def g = new Group()
      o.addToGroups(g)
    }

    when:
    def result = o.canRegisterGroups()

    then:
    !result
  }

  def 'Ensure limited, active org some how over max cant register groups'() {
    setup:
    def o = new Organization()
    o.active = true
    o.groupLimit = 100

    (1..101).each {
      def s = new Group()
      o.addToGroups(s)
    }

    when:
    def result = o.canRegisterGroups()
    
    then:
    !result
  }

  def 'Ensure limited, inactive org that hasnt reached max cant register groups'() {
    setup:
    def o = new Organization()
    o.active = false
    o.groupLimit = 100

    (1..99).each {
      def s = new Group()
      o.addToGroups(s)
    }

    when:
    def result = o.canRegisterGroups()

    then:
    !result
  }

  def 'Ensure limited, inactive org that has reached max cant register groups'() {
    setup:
    def o = new Organization()
    o.active = false
    o.groupLimit = 100

    (1..100).each {
      def s = new Group()
      o.addToGroups(s)
    }

    when:
    def result = o.canRegisterGroups()

    then:
    !result
  }

  def 'Ensure limited, inactive org some how over max cant register groups'() {
    setup:
    def o = new Organization()
    o.active = false
    o.groupLimit = 100

    (1..101).each {
      def s = new Group()
      o.addToGroups(s)
    }

    when:
    def result = o.canRegisterGroups()

    then:
    !result
  }

  def 'Ensure active, non workflow Organization is functioning'() {
    setup:
    def o = new Organization()
    o.active = true
    o.undergoingWorkflow = false

    when:
    def result = o.functioning()

    then:
    result
  }

  def 'Ensure non active, non workflow Organization isnt functioning'() {
    setup:
    def o = new Organization()
    o.active = false
    o.undergoingWorkflow = false

    when:
    def result = o.functioning()

    then:
    !result
  }

  def 'Ensure non active, workflow Organization isnt functioning'() {
    setup:
    def o = new Organization()
    o.active = false
    o.undergoingWorkflow = true

    when:
    def result = o.functioning()

    then:
    !result
  }

  def 'Ensure active, workflow Organization isnt functioning'() {
    setup:
    def o = new Organization()
    o.active = true
    o.undergoingWorkflow = true

    when:
    def result = o.functioning()

    then:
    !result
  }

  def 'Ensure archived Organization isnt functioning'() {
    setup:
    def o = new Organization()
    o.active = true
    o.undergoingWorkflow = false
    o.archived = true

    when:
    def result = o.functioning()

    then:
    !result
  }

  def 'Ensure blocked Organization isnt functioning'() {
    setup:
    def o = new Organization()
    o.active = true
    o.undergoingWorkflow = false
    o.blocked = true

    when:
    def result = o.functioning()

    then:
    !result
  }

  def 'Ensure super administrator can always create Organization'() {
    setup:
    def o = new Organization()
    shiroSubject.isPermitted("app:administrator") >> true

    when:
    def result = o.canCreate()

    then:
    result
  }

  def 'Ensure non super administrator cant create Organization'() {
    setup:
    def o = new Organization()

    when:
    def result = o.canCreate()

    then:
    !result
  }

  def 'Ensure non administrator cant modify Organization'() {
    setup:
    def o = new Organization()

    when:
    def result = o.canMutate()

    then:
    !result
  }

  def 'Ensure super administrator can always modify Organization'() {
    setup:
    def o = Organization(archived:true, blocked:true)
    shiroSubject.isPermitted("app:administrator") >> true

    when:
    def result = o.canMutate()

    then:
    result
  }

  def 'Ensure administrator cant modify Organization when blocked'() {
    setup:
    def o = new Organization(archived:false, blocked:true)
    o.active = true
    shiroSubject.isPermitted("app:manage:organization:${o.id}:edit") >> true

    when:
    def result = o.canMutate()

    then:
    !result
  }

  def 'Ensure administrator cant modify Organization when archived'() {
    setup:
    def o = new Organization(archived:true, blocked:false)
    o.active = true
    shiroSubject.isPermitted("app:manage:organization:${o.id}:edit") >> true

    when:
    def result = o.canMutate()

    then:
    !result
  }

  def 'Ensure administrator can modify Organization when not blocked or archived'() {
    setup:
    def o = new Organization(archived:false, blocked:false)
    o.active = true
    shiroSubject.isPermitted("app:manage:organization:${o.id}:edit") >> true

    when:
    def result = o.canMutate()

    then:
    result
  }

  def 'Ensure super administrator can always delete Organization'() {
    setup:
    def o = new Organization()
    o.blocked = true
    shiroSubject.isPermitted("app:administrator") >> true

    when:
    def result = o.canDelete()

    then:
    result
  }

  def 'Ensure non administrator cant delete Organization'() {
    setup:
    def o = new Organization()

    when:
    def result = o.canDelete()

    then:
    !result
  }
}
