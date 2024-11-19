package aaf.vhr

import aaf.base.identity.Subject
import aaf.vhr.Group
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
}