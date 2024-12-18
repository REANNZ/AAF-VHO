package aaf.vhr

import org.springframework.dao.DataIntegrityViolationException
import org.apache.shiro.SecurityUtils

import aaf.base.identity.Role
import aaf.base.identity.Permission
import aaf.base.identity.Subject
import aaf.vhr.AdminHelper

class GroupController {

  static defaultAction = "list"
  static allowedMethods = [save: "POST", update: "POST", delete: "DELETE"]

  def beforeInterceptor = [action: this.&validGroup, except: ['list', 'create', 'save']]

  def roleService

  def getFilteredList(parameters) {
    def filteredGroups = AdminHelper.getInsiderGroups()
    return Group.list(parameters).intersect(filteredGroups)
  }

  def list() {
    log.info "Action: list, Subject: $subject"
    // If you are not a global admin, only show groups that you are an admin of
    if (AdminHelper.isGlobalAdmin()) {
      [groupInstanceList: Group.list(params), groupInstanceTotal: Group.count()]
    } else {
      def groupList = getFilteredList(params)
      [groupInstanceList: groupList, groupInstanceTotal: groupList.size()]
    }
  }

  def show(Long id) {
    log.info "Action: show, Subject: $subject"
    def groupInstance = Group.get(id)

    def role = Role.findWhere(name:"group:${groupInstance.id}:administrators")
    def subject = Subject.get(SecurityUtils.getSubject()?.getPrincipal())

    // Check if you are a global admin, a group admin, or an organization insider.
    if (AdminHelper.isGlobalAdmin() || subject.roles.contains(role) || AdminHelper.isOrganizationInsider(groupInstance.organization.id as Integer)) {
      return [groupInstance: groupInstance, role:role]
    }

    log.error "User ${subject} is trying to access group ${groupInstance} despite not being an admin!"
    flash.type = 'error'
    flash.message = 'controllers.aaf.vhr.group.show.error'
    redirect action: 'list'
  }

  def create() {
    if(validOrganization()) {
      def groupInstance = new Group()
      groupInstance.organization = Organization.get(params.organization.id)
      if(groupInstance.canCreate(groupInstance.organization)) {
        log.info "Action: create, Subject: $subject"
        [groupInstance: groupInstance]
      }
      else {
        log.warn "Attempt to do administrative Group create by $subject for organization ${params.organization.id} was denied - not permitted by assigned permissions"
        response.sendError 403
      }
    }
  }

  def save() {
    if(validOrganization()) {
      def groupInstance = new Group()
      def organization = Organization.get(params.organization.id)
      groupInstance.organization = organization
      if(groupInstance.canCreate(groupInstance.organization)) {
        bindData(groupInstance, params, [include: ['name', 'description', 'groupScope', 'welcomeMessage']])
        if (!groupInstance.validate()) {
          flash.type = 'error'
          flash.message = 'controllers.aaf.vhr.group.validate.failed'
          render(view: "create", model: [groupInstance: groupInstance])
          return
        }

        groupInstance.welcomeMessage = params.welcomeMessage?.encodeAsSanitizedMarkup()

        if(!organization.canRegisterGroups()) {
          flash.type = 'error'
          flash.message = 'controllers.aaf.vhr.group.licensing.failed'
          render(view: "create", model: [groupInstance: groupInstance])
          return
        }

        organization.addToGroups(groupInstance)

        if (!groupInstance.save()) {
          flash.type = 'error'
          flash.message = 'controllers.aaf.vhr.group.save.failed'
          render(view: "create", model: [groupInstance: groupInstance])
          return
        }

        def groupRole = new Role(name:"group:${groupInstance.id}:administrators", description: "Administrators for the Group ${groupInstance.name} of Organization ${groupInstance.organization.displayName}")
        def groupPermission = new Permission(type: Permission.wildcardPerm, target: "app:manage:organization:${groupInstance.organization.id}:group:${groupInstance.id}:*", role:groupRole)
        groupRole.addToPermissions(groupPermission)
        if(!groupRole.save()) {
          log.error "Unable to save new Role instance to represent admin rights for ${groupInstance}"
          org.errors.each { error ->
            log.error error
          }
        }

        log.info "Action: save, Subject: $subject, Object: $groupInstance"
        flash.type = 'success'
        flash.message = 'controllers.aaf.vhr.group.save.success'
        redirect(action: "show", id: groupInstance.id)
      }
      else {
        log.warn "Attempt to do administrative Group save by $subject for organization ${params.organization.id} was denied - not permitted by assigned permissions"
        response.sendError 403
      }
    }
  }

  def edit(Long id) {
    def groupInstance = Group.get(id)
    if(groupInstance.canMutate()) {
      log.info "Action: edit, Subject: $subject, Object: groupInstance"
      [groupInstance: groupInstance]
    }
    else {
      log.warn "Attempt to do administrative Group edit by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def update(Long id, Long version) {
    def groupInstance = Group.get(id)
    if(groupInstance.canMutate()) {
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.update.noversion'
        render(view: "edit", model: [groupInstance: groupInstance])
        return
      }

      if (groupInstance.version > version) {
        groupInstance.errors.rejectValue("version", "controllers.aaf.vhr.group.update.optimistic.locking.failure")
        render(view: "edit", model: [groupInstance: groupInstance])
        return
      }

      bindData(groupInstance, params, [include: ['name', 'description', 'groupScope', 'welcomeMessage']])
      if (!groupInstance.validate()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.validate.failed'
        render(view: "edit", model: [groupInstance: groupInstance])
        return
      }

      groupInstance.welcomeMessage = params.welcomeMessage?.encodeAsSanitizedMarkup()

      if (!groupInstance.save()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.update.failed'
        render(view: "edit", model: [groupInstance: groupInstance])
        return
      }

      log.info "Action: update, Subject: $subject, Object: $groupInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.group.update.success'
      redirect(action: "show", id: groupInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Group update by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def delete(Long id) {
    def groupInstance = Group.get(id)
    if(groupInstance.canDelete()) {
      try {
        def groupRole = Role.findByName("group:${groupInstance.id}:administrators")
        if (groupRole) roleService.deleteRole(groupRole)

        groupInstance.delete()

        log.info "Action: delete, Subject: $subject, Object: groupInstance"
        flash.type = 'success'
        flash.message = 'controllers.aaf.vhr.group.delete.success'
        redirect(controller:"organization", action: "show", id: groupInstance.organization.id, fragment:"tab-groups")
      }
      catch (DataIntegrityViolationException e) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.delete.failure'
        redirect(action: "show", id: id)
      }
    }
    else {
      log.warn "Attempt to do administrative Group delete by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def toggleActive(Long id, Long version) {
    def groupInstance = Group.get(id)
    if(groupInstance.canMutate()) {
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.toggleactive.noversion'
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      if (groupInstance.version > version) {
        groupInstance.errors.rejectValue("version", "controllers.aaf.vhr.group.toggleactive.optimistic.locking.failure")
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      groupInstance.active = !groupInstance.active

      if (!groupInstance.save()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.toggleactive.failed'
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      log.info "Action: toggleActive, Subject: $subject, Object: $groupInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.group.toggleactive.success'
      redirect(action: "show", id: groupInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Group toggleactive by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def toggleBlocked(Long id, Long version) {
    def groupInstance = Group.get(id)
    if(SecurityUtils.subject.isPermitted("app:administrator")) {
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.toggleblocked.noversion'
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      if (groupInstance.version > version) {
        groupInstance.errors.rejectValue("version", "controllers.aaf.vhr.group.toggleblocked.optimistic.locking.failure")
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      groupInstance.blocked = !groupInstance.blocked

      if (!groupInstance.save()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.toggleblocked.failed'
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      log.info "Action: toggleActive, Subject: $subject, Object: $groupInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.group.toggleblocked.success'
      redirect(action: "show", id: groupInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Group toggleblocked by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def toggleArchived(Long id, Long version) {
    def groupInstance = Group.get(id)
    if(SecurityUtils.subject.isPermitted("app:administrator")) {
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.togglearchived.noversion'
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      if (groupInstance.version > version) {
        groupInstance.errors.rejectValue("version", "controllers.aaf.vhr.group.togglearchived.optimistic.locking.failure")
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      groupInstance.archived = !groupInstance.archived

      if (!groupInstance.save()) {
        flash.type = 'error'
        flash.message = 'controllers.aaf.vhr.group.togglearchived.failed'
        render(view: "show", model:[groupInstance: groupInstance])
        return
      }

      log.info "Action: toggleActive, Subject: $subject, Object: $groupInstance"
      flash.type = 'success'
      flash.message = 'controllers.aaf.vhr.group.togglearchived.success'
      redirect(action: "show", id: groupInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Group togglearchived by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def enforcetwosteplogin(Long id, boolean enforce) {
    def groupInstance = Group.get(id)
    if(groupInstance.canMutate()) {
      groupInstance.totpForce = enforce

      if(!groupInstance.save()) {
        log.warn "Failed to save change of two-step enforce for $groupInstance"
        response.sendError 500
        return
      }

      log.info "Action: enforcetwosteplogin, Subject: $subject, Object: $groupInstance"
      flash.type = 'success'
      if(enforce)
        flash.message = 'controllers.aaf.vhr.group.enforcetwosteplogin.enable.success'
      else
        flash.message = 'controllers.aaf.vhr.group.enforcetwosteplogin.disable.success'
      redirect(action: "show", id: groupInstance.id)
    }
    else {
      log.warn "Attempt to do administrative Group enforcetwosteplogin by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def nonfinalized(Long id) {
    def groupInstance = Group.get(id)
    if(groupInstance.canMutate()) {
      log.info "Action: nonfinalized, Subject: $subject, Object: groupInstance"
      [groupInstance: groupInstance]
    }
    else {
      log.warn "Attempt to access administrative Group nonfinalize by $subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  private validOrganization() {
    if(!params.organization?.id) {
      log.warn "Organization ID was not present"

      flash.type = 'info'
      flash.message = message(code: 'controllers.aaf.vhr.groups.organization.no.id')

      redirect action:'list'
      return false
    }

    def organizationInstance = Organization.get(params.organization.id)
    if (!organizationInstance) {
      log.warn "organizationInstance was not a valid instance"

      flash.type = 'info'
      flash.message = 'controllers.aaf.vhr.groups.organization.notfound'

      redirect action:'list'
      return false
    }

    true
  }

  private validGroup() {
    if(!params.id) {
      log.warn "ID was not present"

      flash.type = 'info'
      flash.message = message(code: 'controllers.aaf.vhr.group.no.id')

      redirect action:'list'
      return false
    }

    def groupInstance = Group.get(params.id)
    if (!groupInstance) {
      log.warn "groupInstance was not a valid instance"

      flash.type = 'info'
      flash.message = 'controllers.aaf.vhr.group.notfound'

      redirect action:'list'
      return false
    }

    true
  }
}
