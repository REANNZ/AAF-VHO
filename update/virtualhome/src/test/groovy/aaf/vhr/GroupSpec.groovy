package aaf.vhr

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import test.shared.ShiroEnvironment

import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class GroupSpec extends Specification implements DomainUnitTest<Group>  {

  @Shared def shiroEnvironment = new ShiroEnvironment()

  org.apache.shiro.subject.Subject shiroSubject
  
  def cleanupSpec() { 
    shiroEnvironment.tearDownShiro() 
  }

  def setup() {
    shiroSubject = Mock(org.apache.shiro.subject.Subject)
    shiroEnvironment.setSubject(shiroSubject)
  }

  def 'name is required to be valid'() {
    setup:
    def g = new Group()
    mockForConstraintsTests(Group, [g])

    when:
    g.name = val
    def result = g.validate()

    then:
    result == expected

    if (!expected)
      reason == g.errors['name']

    where:
    val << [null, '', 'name']
    expected << [false, false, true]
    reason << ['null', 'blank', '']
  }

  def 'description is required to be valid'() {
    setup:
    def g = new Group()
    mockForConstraintsTests(Group, [g])

    when:
    g.description = val
    def result = g.validate()

    then:
    result == expected

    if (!expected)
      reason == g.errors['description']

    where:
    val << [null, '', 'name']
    expected << [false, false, true]
    reason << ['null', 'blank', '']
  }

  def 'functioning when active and Organization functioning' () {
    setup:
    def g = new Group()

    when:
    g.active = true
    g.organization.active = true

    then:
    g.functioning()
  }

  def 'not functioning when inactive and Organization functioning' () {
    setup:
    def g = new Group()

    when:
    g.active = false
    g.organization.active = true

    then:
    !g.functioning()
  }

  def 'not functioning when inactive and Organization not functioning' () {
    setup:
    def g = new Group()

    when:
    g.active = false
    g.organization.active = false

    then:
    !g.functioning()
  }

  def 'not functioning when active but Organization not functioning' () {
    setup:
    def g = new Group()

    when:
    g.active = true
    g.organization.active = false

    then:
    !g.functioning()
  }

  def 'not functioning when blocked' () {
    setup:
    def g = new Group()
    g.organization.active = true

    expect:
    g.functioning()

    when:
    g.blocked = true

    then:
    !g.functioning()
  }

  def 'not functioning when archived' () {
    setup:
    def g = new Group()
    g.organization.active = true

    expect:
    g.functioning()

    when:
    g.archived = true

    then:
    !g.functioning()
  }

  def 'Ensure super administrator can always create Group'() {
    setup:
    def o = new Organization()
    def g = new Group(organization:o)
    g.blocked = true
    shiroSubject.isPermitted("app:administrator") >> true

    when:
    def result = g.canCreate(o)

    then:
    result
  }

  def 'Ensure non administrator cant create Group'() {
    setup:
    def o = new Organization()
    def g = new Group(organization:o)

    when:
    def result = g.canCreate(o)

    then:
    !result
  }

  def 'Ensure administrator can create Group'() {
    setup:
    def o = new Organization()
    def g = new Group(organization:o)
    g.organization.active = true
    shiroSubject.isPermitted("app:manage:organization:${g.organization.id}:group:create") >> true

    when:
    def result = g.canCreate(o)

    then:
    result
  }

  def 'Ensure administrator cant create Group if owner is not functioning'() {
    setup:
    def o = new Organization()
    o.blocked = true
    def g = new Group(organization:o)
    shiroSubject.isPermitted("app:manage:organization:${g.organization.id}:group:create") >> true

    when:
    def result = g.canCreate(o)

    then:
    !result
  }

  def 'Ensure non administrator cant modify Group'() {
    setup:
    def g = new Group()

    when:
    def result = g.canMutate()

    then:
    !result
  }

  def 'Ensure super administrator can always modify Group'() {
    setup:
    def g = new Group(archived:true, blocked:true)
    shiroSubject.isPermitted("app:administrator") >> true

    when:
    def result = g.canMutate()

    then:
    result
  }

  def 'Ensure administrator cant modify Group when blocked'() {
    setup:
    def g = new Group(archived:false, blocked:true)
    g.organization.active = true
    shiroSubject.isPermitted("app:manage:organization:${g.organization.id}:group:${g.id}:edit") >> true

    when:
    def result = g.canMutate()

    then:
    !result
  }

  def 'Ensure administrator cant modify Group when archived'() {
    setup:
    def g = new Group(archived:true, blocked:false)
    g.organization.active = true
    shiroSubject.isPermitted("app:manage:organization:${g.organization.id}:group:${g.id}:edit") >> true

    when:
    def result = g.canMutate()

    then:
    !result
  }

  def 'Ensure administrator cant modify Group when owner cant be modified'() {
    setup:
    def g = new Group(archived:false, blocked:false)
    g.organization.active = true
    g.organization.blocked = true
    shiroSubject.isPermitted("app:manage:organization:${g.organization.id}:group:${g.id}:edit") >> true

    when:
    def result = g.canMutate()

    then:
    !result
  }

  def 'Ensure administrator can modify Group when not blocked or archived'() {
    setup:
    def g = new Group(archived:false, blocked:false)
    g.organization.active = true
    shiroSubject.isPermitted("app:manage:organization:${g.organization.id}:group:${g.id}:edit") >> true

    when:
    def result = g.canMutate()

    then:
    result
  }

  def 'Ensure super administrator can always delete Group'() {
    setup:
    def g = new Group()
    g.organization.blocked = true
    shiroSubject.isPermitted("app:administrator") >> true

    when:
    def result = g.canDelete()

    then:
    result
  }

  def 'Ensure non administrator cant delete Group'() {
    setup:
    def g = new Group()

    when:
    def result = g.canDelete()

    then:
    !result
  }

  def 'ensure correct response for enforceTwoStepLogin'() {
    setup:
    def g = new Group(totpForce:force)

    expect:
    g.enforceTwoStepLogin() == force

    where:
    force << [true, false]
  }

}
