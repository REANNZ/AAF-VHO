package aaf.base.identity

import grails.test.mixin.*
import grails.buildtestdata.mixin.Build
import spock.lang.*
import grails.test.spock.*

@TestFor(aaf.base.identity.PermissionService)
@Mock([Subject, Role, Permission])
@Build([Subject, Role, Permission])
class RoleServiceSpec extends spock.lang.Specification {

  def "successfully creates unprotected role"() {
    setup:
    def rs = new RoleService()
    
    when:
    def role = rs.createRole('role', 'role description', false)

    then:
    role.id > 0
    role.name == 'role'
    role.description == 'role description'
    !role.protect
    role.permissions == null
    role.subjects == null

    Role.count() == 1
  }

  def "successfully creates protected role"() {
    setup:
    def rs = new RoleService()
    
    when:
    def role = rs.createRole('role', 'role description', true)

    then:
    role.id > 0
    role.name == 'role'
    role.description == 'role description'
    role.protect
    role.permissions == null
    role.subjects == null

    Role.count() == 1
  }

  def "when an error occurs persisting Role a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    Role.metaClass.save = { null }
    
    when:
    rs.createRole('role', 'role description', false)

    then:
    RuntimeException e = thrown()
    e.message.contains "Error creating new role, object persistance failed"
  }

  def "when invalid data is presented a role object populated with errors is returned"() {
    setup:
    def rs = new RoleService()
    
    when:
    def role = rs.createRole(null, null, false)

    then:
    Role.count() == 0
    role.hasErrors()
  }

  def "when a duplicate role is attempted to be created a role object populated with errors is returned"() {
    setup:
    def rs = new RoleService()
    rs.createRole('role', 'role description', false)
    simpleDatastore.currentSession.flush()
    
    when:
    def role = rs.createRole('role', 'role description', false)

    then:
    Role.count() == 1
    role.hasErrors()
  }

  def "when a role is being deleted and a subject has a save error a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()
    role.addToSubjects(subject).save()
    subject.addToRoles(role).save()

    subject.metaClass.save = { null }

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects.size() == 1
    subject.roles.size() == 1
    
    when:
    rs.deleteRole(role)

    then:
    Role.count() == 1
    Subject.count() == 1
    role.refresh().subjects.size() == 1
    subject.refresh().roles.size() == 1
    RuntimeException e = thrown()
    e.message == "Error updating $subject to remove $role"
  }

  def "a role can be successfully deleted"() {
    setup:
    def rs = new RoleService()
    
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build(principal: "principal1")
    def subject2 = Subject.build(principal: "principal2")

    role.addToSubjects(subject).save()
    role.addToSubjects(subject2).save()

    subject.addToRoles(role).save()
    subject2.addToRoles(role).save()

    expect:
    Role.count() == 1
    Subject.count() == 2
    role.subjects.size() == 2
    subject.roles.size() == 1
    subject2.roles.size() == 1
    
    when:
    rs.deleteRole(role)

    then:
    Role.count() == 0
    Subject.count() == 2
    subject.roles.size() == 0
    subject2.roles.size() == 0
  }

  def "when a Role fails update a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    role.metaClass.save = { null }
    
    when:
    rs.updateRole(role)

    then:
    RuntimeException e = thrown()
    e.message.contains "Error updating $role"
  }

  def "When a Role is updated a valid Role is returned containing no errors"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    
    expect:
    Role.count() == 1
    role.name == 'role'
    
    when:
    role.name = 'new role'
    def newRole =rs.updateRole(role)

    then:
    Role.count() == 1
    role.id == newRole.id
    !newRole.hasErrors()
    newRole.name == 'new role'
  }

  def "when a Role fails save while add a new subject to a Role a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()
    role.metaClass.save = { null }

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects == null
    
    when:
    rs.addMember(subject, role)

    then:
    RuntimeException e = thrown()
    e.message.contains "Error updating $role to add $subject"
    role.refresh().subjects.size() == 0
  }

  def "when a Subject fails save while add a new subject a Role a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()
    subject.metaClass.save = { null }

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects == null
    
    when:
    rs.addMember(subject, role)

    then:
    RuntimeException e = thrown()
    e.message.contains "Error updating $subject when adding to $role"
    role.refresh().subjects.size() == 0
  }

  def "Can successfully add a new subject to a role"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects == null
    subject.roles == null
    
    when:
    rs.addMember(subject, role)

    then:
    Role.count() == 1
    Subject.count() == 1
    role.refresh().subjects.size() == 1
    subject.refresh().roles.size() == 1
  }

  def "If role fails to save when removing a subject from a Role a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()
    rs.addMember(subject, role)
    role.metaClass.save = { null }

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects.size() == 1
    subject.roles.size() == 1
    
    when:
    rs.deleteMember(subject, role)

    then:
    Role.count() == 1
    Subject.count() == 1
    RuntimeException e = thrown()
    e.message == "Error updating $role to delete $subject"
  }

  def "If subject fails to save when removing a subject from a Role a RuntimeException is thrown to rollback transaction"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()
    rs.addMember(subject, role)
    subject.metaClass.save = { null }

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects.size() == 1
    subject.roles.size() == 1
    
    when:
    rs.deleteMember(subject, role)

    then:
    Role.count() == 1
    Subject.count() == 1
    RuntimeException e = thrown()
    e.message == "Error updating $subject to delete from $role"
  }

  def "Can successfully delete a subject from a role"() {
    setup:
    def rs = new RoleService()
    def role = rs.createRole('role', 'role description', false)
    def subject = Subject.build()
    rs.addMember(subject, role)

    expect:
    Role.count() == 1
    Subject.count() == 1
    role.subjects.size() == 1
    subject.roles.size() == 1
    
    when:
    rs.deleteMember(subject, role)
    def r2 = Role.get(role.id)  // .refresh() seems to be buggy for hasMany delete
    def s2 = Subject.get(subject.id)

    then:
    Role.count() == 1
    Subject.count() == 1
    r2.subjects.size() == 0
    s2.roles.size() == 0
  }

}
