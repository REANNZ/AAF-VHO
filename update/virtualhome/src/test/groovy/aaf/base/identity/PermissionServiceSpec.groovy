package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import grails.testing.services.ServiceUnitTest

class PermissionServiceSpec extends Specification implements ServiceUnitTest<PermissionService> {

  def 'ensure error saving permissions throws RuntimeException to rollback transaction state'() {
    setup:
    def ps = new PermissionService()
    def owner = new Subject()
    def permission = new Permission()
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
    def owner = new Subject()
    def permission = new Permission()
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
    def owner = new Subject()
    def permission = new Permission()

    when:
    def savedPermission = ps.createPermission(permission, owner)

    then:
    owner.permissions.size() == 1
    owner.permissions.contains savedPermission
  }

  def 'valid permission is correctly added to Role owner'() {
    setup:
    def ps = new PermissionService()
    def owner = new Role()
    def permission = new Permission()

    when:
    def savedPermission = ps.createPermission(permission, owner)

    then:
    owner.permissions.size() == 1
    owner.permissions.contains savedPermission
  }

  def 'ensure error when saving owner during deleting permission throws RuntimeException to rollback transaction state'() {
    setup:
    def ps = new PermissionService()
    def owner = new Subject()
    def permission = new Permission()
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
    def owner = new Subject()
    def permission = new Permission()
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
    def owner = new Role()
    def permission = new Permission()
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
