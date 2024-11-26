package aaf.vhr

import aaf.base.identity.Subject
import aaf.vhr.Group
import aaf.vhr.Organization
import org.apache.shiro.SecurityUtils

class AdminHelper {
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

    // This is all groups that we are an admin of, plus all groups in any organization that owns a group we are an admin of.
    static def getInsiderGroups() {
        def groups = getAdminGroups()
        def insiderGroups = []
        // Check the org for each group and find any other groups present
        groups.each { group ->
            group.organization.groups.each { orgGroup ->
                insiderGroups.add(orgGroup)
            }
        }

        return insiderGroups.unique()
    }

    static def getAdminOrganisations() {
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

        return false
    }
}