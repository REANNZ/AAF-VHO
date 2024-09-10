
import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import org.apache.shiro.authz.permission.WildcardPermissionResolver
import org.apache.shiro.authz.permission.WildcardPermission

import aaf.base.identity.Subject
import aaf.base.identity.Role
import aaf.base.identity.Permission

class AccessControlRealmSpec extends IntegrationSpec {
  
  def 'positive result when subject has the required role'() {
    setup:
    def realm = new AccessControlRealm()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.save()
    
    when:
    def hasRole = realm.hasRole(subject.id, 'testrole')
    
    then:
    hasRole
  }
  
  def 'negative result when subject doesnt have the required role'() {
    setup:
    def realm = new AccessControlRealm()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.save()
    
    when:
    def hasRole = realm.hasRole(subject.id, 'testrole2')
    
    then:
    !hasRole
  }
  
  def 'positive result when subject has all the required roles'() {
    setup:
    def realm = new AccessControlRealm()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.save()
    
    def role2 = new Role(name:'testrole2').save()
    role2.addToSubjects(subject)
    role2.save()
    
    when:
    def hasRole = realm.hasAllRoles(subject.id, ["testrole", "testrole2"])
    
    then:
    hasRole
  }
  
  def 'negative result when subject doesnt have all the required roles'() {
    setup:
    def realm = new AccessControlRealm()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.save()
    
    when:
    String[] roles = ['testrole', 'testrole2']
    def hasRole = realm.hasAllRoles(subject.id, roles)
    
    then:
    !hasRole
  }
  
  def 'positive result when subject has directly assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    subject.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:id'))
    
    then:
    isPermitted
  }
  
  def 'negative result when subject isnt directly assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    subject.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action2:id'))
    
    then:
    !isPermitted
  }
  
  def 'positive result when subject has wildcard assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:*")))
    subject.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:id'))
    
    then:
    isPermitted
  }
  
  def 'negative result when subject isnt wildcard assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:*")))
    subject.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain2:action2:id'))
    
    then:
    !isPermitted
  }
  
  def 'positive result when subject has role that has directly assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:id'))
    
    then:
    isPermitted
  }
  
  def 'negative result when subject has role but neither are directly assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action2:id'))
    
    then:
    !isPermitted
  }
  
  def 'positive result when subject and role have directly assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    subject.save()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:id'))
    
    then:
    isPermitted
  }
  
  def 'positive result when subject has role that has wildcard assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:*")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:id'))
    
    then:
    isPermitted
  }
  
  def 'negative result when subject has role but neither are wildcard assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:*")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain2:action:id'))
    
    then:
    !isPermitted
  }
  
  def 'positive result when subject and role have wildcard assigned permission'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:*")))
    subject.save()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:*")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:id'))
    
    then:
    isPermitted
  }
  
  def 'positive result when subject and role have multiple permissions only 1 of which matches'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain:action:*")))
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain2:action:*")))
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:read")))
    subject.save()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:*")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain:action:*")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain2:action:*")))
    role.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:read'))
    
    then:
    isPermitted
  }
  
  def 'positive result when subject has multiple roles that have multiple permissions only 1 of which matches'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    subject.save()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain:action:*")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain:action:*")))
    role.save()
    
    def role2 = new Role(name:'testrole2').save()
    role2.addToSubjects(subject)
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain2:action:*")))
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain2:action:id")))
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain2:action:*")))
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain2:action:*")))
    role2.save()
    
    def role3 = new Role(name:'testrole3').save()
    role3.addToSubjects(subject)
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain3:action:*")))
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain3:action:id")))
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain3:action:*")))
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:read")))
    role3.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:read'))
    
    then:
    isPermitted
  }
  
  def 'negative result when subject has multiple roles that have multiple permissions of which none match'() {
    setup:
    def realm = new AccessControlRealm()
    realm.shiroPermissionResolver = new WildcardPermissionResolver()
    def subject = Subject.build()
    subject.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    subject.save()
    def role = new Role(name:'testrole').save()
    role.addToSubjects(subject)
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:id")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain:action:*")))
    role.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain:action:*")))
    role.save()
    
    def role2 = new Role(name:'testrole2').save()
    role2.addToSubjects(subject)
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain2:action:*")))
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain2:action:id")))
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain2:action:*")))
    role2.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain2:action:*")))
    role2.save()
    
    def role3 = new Role(name:'testrole3').save()
    role3.addToSubjects(subject)
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain3:action:*")))
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain3:action:id")))
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:otherdomain3:action:*")))
    role3.addToPermissions(new Permission(type:Permission.defaultPerm, target:("test:domain:action:read")))
    role3.save()
    
    when:
    def isPermitted = realm.isPermitted(subject.id, new WildcardPermission('test:domain:action:write'))
    
    then:
    !isPermitted
  }
  
}
