package aaf.vhr

import org.apache.shiro.SecurityUtils
import aaf.base.identity.Role
import aaf.base.identity.Subject

class ManageAdministratorsController {
  
  def search() {
    def role = validRole()
    if(role) {
      def subjects = Subject.list()

      if(role.subjects)
        subjects.removeAll(role.subjects)

      render (template: "/templates/manageadministrators/search", model:[subjects:subjects, role:role])
    }
  }

  def add() {
    def role = validRole()
    def targetSubject = validSubject()
    if(role && targetSubject) {
      role.addToSubjects(targetSubject)
      targetSubject.addToRoles(role)

      if(!role.save()) {
        log.warn "Faied to save ${role} when trying to add ${targetSubject} as administrator"
        response.sendError 500
      }

      if(!targetSubject.save()) {
        log.warn "Faied to save ${targetSubject} when trying to add to ${role} as administrator"
        response.sendError 500
      }

      render (template: "/templates/manageadministrators/administrators", model:[role:role])
    }
  }

  def remove() {
    def role = validRole()
    def targetSubject = validSubject()
    if(role && targetSubject) {
      role.removeFromSubjects(targetSubject)
      targetSubject.removeFromRoles(role)

      if(!role.save()) {
        log.warn "Faied to save ${role} when trying to remove ${targetSubject} as administrator"
        response.sendError 500
      }

      if(!targetSubject.save()) {
        log.warn "Faied to save ${targetSubject} when trying to remove from ${role} as administrator"
        response.sendError 500
      }

      render (template: "/templates/manageadministrators/administrators", model:[role:role])
    }
  }

  private aaf.base.identity.Subject validSubject() {
    if(!params.subjectID) {
      log.warn "subjectID was not present"
      response.sendError 400
      return null
    }

    def targetSubject = Subject.get(params.subjectID)
    if (!targetSubject) {
      log.warn "No subject for $params.subjectID located when attempting to removemember"
      response.sendError 400
      return null
    }

    return targetSubject
  }

  private aaf.base.identity.Role validRole() {
    if(!params.type) {
      log.warn "Type was not present"
      response.sendError 400
      return null
    }
    if(!params.id) {
      log.warn "ID was not present"
      response.sendError 400
      return null
    }


    def role
    switch(params.type) {
      case "organization":
                          def organizationInstance = Organization.get(params.id)
                          if (!organizationInstance) {
                            log.warn "organizationInstance was not a valid instance"
                            response.sendError 400
                            return null
                          }
                          if(SecurityUtils.subject.isPermitted("app:manage:organization:${organizationInstance.id}:manage:administrators")) {
                            role = Role.findWhere(name:"organization:${organizationInstance.id}:administrators")
                            if (!role) {
                              log.warn "role was not a valid instance"
                              response.sendError 400
                              return null
                            }
                          } else {
                            log.warn "Attempt to change administrators associated with $organizationInstance by $subject was denied - not permitted by assigned permissions"
                            response.sendError 403
                            return null
                          }

                          return role
                          break

      case "group":
                          def groupInstance = Group.get(params.id)
                          if (!groupInstance) {
                            log.warn "groupInstance was not a valid instance"
                            response.sendError 400
                            return null
                          }
                          if(SecurityUtils.subject.isPermitted("app:manage:organization:${groupInstance.organization.id}:group:${groupInstance.id}:manage:administrators")) {
                            role = Role.findWhere(name:"group:${groupInstance.id}:administrators")
                            if (!role) {
                              log.warn "role was not a valid instance"
                              response.sendError 400
                              return null
                            }
                          } else {
                            log.warn "Attempt to change administrators associated with $groupInstance by $subject was denied - not permitted by assigned permissions"
                            response.sendError 403
                            return null
                          }

                          return role
                          break

      default:            log.warn "Attempt to change administrators of unknown type by $subject was denied"
                          response.sendError 403
                          return null
    }
  }

}