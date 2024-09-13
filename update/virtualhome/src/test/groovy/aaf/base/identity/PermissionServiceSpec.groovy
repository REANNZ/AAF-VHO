package aaf.base.identity

import grails.test.mixin.*
import grails.buildtestdata.mixin.Build
import spock.lang.*
import grails.test.spock.*

@TestFor(aaf.base.identity.PermissionService)
@Mock([Subject, Role, Permission])
@Build([Subject, Role, Permission])
class PermissionServiceSpec extends Specification {

  def 'ensure error saving permissions throws RuntimeException to rollback transaction state'() {
    setup:
    def ps = new PermissionService()
    def owner = Subject.build()
    def permission = Permission.build()
    permission.metaClass.save = { null }

    when:
    ps.createPermission(permission, owner)

    then:
    RuntimeException e = thrown()
    e.message == "Unable to persist new permission"
  }

  def 'ensure error saving owner throws RuntimeException to rollback transaction state'() {
    setup:
    def ps = new PermissionService()
    def owner = Subject.build()
    def permission = Permission.build()
    owner.metaClass.save = { null }

    when:
    ps.createPermission(permission, owner)

    then:
    RuntimeException e = thrown()
    e.message.contains "Unable to add permission"
  }

  def 'valid permission is correctly added to Subject owner'() {
    setup:
    def ps = new PermissionService()
    def owner = Subject.build()
    def permission = Permission.build()

    when:
    def savedPermission = ps.createPermission(permission, owner)

    then:
    owner.permissions.size() == 1
    owner.permissions.contains savedPermission
  }

  def 'valid permission is correctly added to Role owner'() {
    setup:
    def ps = new PermissionService()
    def owner = Role.build()
    def permission = Permission.build()

    when:
    def savedPermission = ps.createPermission(permission, owner)

    then:
    owner.permissions.size() == 1
    owner.permissions.contains savedPermission
  }

  def 'ensure error when saving owner during deleting permission throws RuntimeException to rollback transaction state'() {
    setup:
    def ps = new PermissionService()
    def owner = Subject.build()
    def permission = Permission.build()
    permission.owner = owner
    permission.save()
    owner.addToPermissions(permission)
    owner.save()

    owner.metaClass.save = { null }

    when:
    ps.deletePermission(permission)

    then:
    RuntimeException e = thrown()
    e.message == "Unable to remove permission $permission from $owner"
  }

  def 'valid permission is successfully deleted from Subject'() {
    setup:
    def ps = new PermissionService()
    def owner = Subject.build()
    def permission = Permission.build()
    permission.owner = owner
    permission.save()
    owner.addToPermissions(permission)
    owner.save()

    def before_count = owner.permissions.size()
    def before_contained = owner.permissions.contains permission

    when:
    ps.deletePermission(permission)

    then:
    before_count == 1
    before_contained
    owner.permissions.size() == 0
  }

  def 'valid permission is successfully deleted from Role'() {
    setup:
    def ps = new PermissionService()
    def owner = Role.build()
    def permission = Permission.build()
    permission.owner = owner
    permission.save()
    owner.addToPermissions(permission)
    owner.save()

    def before_count = owner.permissions.size()
    def before_contained = owner.permissions.contains permission

    when:
    ps.deletePermission(permission)

    then:
    before_count == 1
    before_contained
    owner.permissions.size() == 0
  }

}
