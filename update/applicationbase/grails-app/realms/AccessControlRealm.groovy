import org.apache.shiro.authz.permission.WildcardPermission

import aaf.base.identity.Subject

class AccessControlRealm {

  def credentialMatcher
  def shiroPermissionResolver

  def hasRole(principal, roleName) {
    def roles = Subject.withCriteria {
      roles {
        eq("name", roleName)
      }
      eq("id", principal)
    }

    return roles.size() > 0
  }

  def hasAllRoles(principal, roleNames) {
    def r = Subject.withCriteria {
      roles {
        'in'("name", roleNames)
      }
      eq("id", principal)
    }

    return r.size() == roleNames.size()
  }

  def isPermitted(principal, requiredPermission) {      
    // Required permission directly applied to the subject
    def subject = Subject.get(principal)
    def permissions = subject.permissions

    def permitted = permissions?.find { ps ->
      def perm = shiroPermissionResolver.resolvePermission(ps.target)

      if (perm.implies(requiredPermission))
          return true
      else
          return false
    }

    if (permitted != null) { return true }

    // Required permission applied to a role of which the subject has membership
    def results = Subject.executeQuery("select distinct p from Subject as subject join subject.roles as role join role.permissions as p where subject.id = '$principal'")

    permitted = results.find { ps ->
      def perm = shiroPermissionResolver.resolvePermission(ps.target)

      if (perm.implies(requiredPermission))
          return true
      else
          return false
    }

    if (permitted != null) { return true }
    else { return false }
  }
}
