package aaf.vhr

import aaf.base.identity.Subject
import aaf.base.identity.Role
import aaf.vhr.Group
import aaf.vhr.Organization
import org.apache.shiro.SecurityUtils

class AdminHelper {

    static def isGlobalAdmin() {
        return SecurityUtils.subject.isPermitted("app:administration")
    }

    static def getAdminGroups() {
        def adminList = []

        def subject = Subject.get(SecurityUtils.getSubject()?.getPrincipal())
        subject.roles.each { role ->
            def roleComponents = role.name.split(':')
            if (roleComponents.size() == 3 && roleComponents[0] == "group" && roleComponents[2] == "administrators") {
                def id = roleComponents[1] as Integer
                adminList.add(Group.get(id))
            }
        }

        return adminList
    }

    // An insider group is a group where:
    // (1) You are the admin of the group.
    // (2) You are an admin of an organization the group is a part of.
    // (3) You are not the admin of the group, but are an admin of another group in the same organization.
    static def getInsiderGroups() {

        def insiderGroups = []

        // Condition (2)
        getAdminOrganizations().each { adminOrg ->
            insiderGroups += adminOrg.groups
        }

        // Condition (1) and (3)
        getAdminGroups().each { group ->
            group.organization.groups.each { orgGroup ->
                insiderGroups.add(orgGroup)
            }
        }

        return insiderGroups.unique()
    }

    static def isOrganizationAdmin(Integer organizationID) {
        def subject = Subject.get(SecurityUtils.getSubject()?.getPrincipal())
        def adminRole = Role.findWhere(name:"organization:${organizationID}:administrators")
        return subject.roles.contains(adminRole)
    }

    static def getAdminOrganizations() {
        def adminList = []

        def subject = Subject.get(SecurityUtils.getSubject()?.getPrincipal())
        subject.roles.each { role ->
            def roleComponents = role.name.split(':')
            if (roleComponents.size() == 3 && roleComponents[0] == "organization" && roleComponents[2] == "administrators") {
                def id = roleComponents[1] as Integer
                adminList.add(Organization.get(id))
            }
        }

        return adminList
    }

    static def isOrganizationInsider(Integer organizationID) {
        def orgGroups = Organization.get(organizationID).groups
        def adminGroups = getAdminGroups()
        if (orgGroups.intersect(adminGroups))
            return true

        return isOrganizationAdmin(organizationID)
    }
}
