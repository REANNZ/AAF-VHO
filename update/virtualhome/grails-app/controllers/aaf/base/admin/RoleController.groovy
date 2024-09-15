package aaf.base.admin

import aaf.base.identity.*

class RoleController {
  static defaultAction = "list"
  
  static allowedMethods = [save: "POST", update: "POST", delete: "DELETE", createpermission: "POST", deletepermission:"POST", addmember:"POST", removemember:"POST"]

  def roleService
  def permissionService

  def beforeInterceptor = [action: this.&validRole, except: ['list', 'create', 'save', 'finalization', 'finalizationerror']]

  def list() {
    [roles: Role.list(),  roleTotal: Role.count()]
  }

  def create() {
    [role: new Role(params)]
  }

  def save() {
    def role = roleService.createRole(params.name, params.description, params.protect == 'on')
    if(role && !role.hasErrors()) {
      flash.type = 'success'
      flash.message = 'controllers.aaf.base.admin.role.create.success'
      redirect(action: "show", id: role.id)
    }
    else
      render view:'create', model:[role:role]
  }

  def show() {
    def role = Role.get(params.id)

    def subjects = Subject.list()
    subjects.removeAll(role.subjects)
    [role:role, subjects: subjects]
  }

  def edit() {
    def role = Role.get(params.id)

    [role:role]
  }

  def update() {
    def role = Role.get(params.id)

    role.name = params.name
    role.description = params.description
    role.protect = params.protect == 'on'

    if(role.validate()) {
      def updatedRole = roleService.updateRole(role)

      flash.type = 'success'
      flash.message = 'controllers.aaf.base.admin.role.update.success'
      redirect(action: "show", id:  updatedRole.id)
    } else {
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.role.update.failed'
      render view:'edit', model:[role:role]
    }
  }

  def delete() {
    def role = Role.get(params.id)

    roleService.deleteRole(role)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.role.delete.success'

    redirect(action: "list")
  }

  def searchNewMembers() {
    def role = Role.get(params.id)
    def subjects = Subject.list()

    render template: "/templates/role/searchnewmembers", plugin: 'administration', model:[subjects:subjects, role:role]
  }

  def addmember() {
    def role = Role.get(params.id)

    def subject = Subject.get(params.subjectID)
    if (!subject) {
      log.warn "No subject for $params.subjectID located when attempting to addmember"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.role.addmember.nosubject'

      redirect(action: "list")
      return
    }

    roleService.addMember(subject, role)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.role.addmember.success'
    redirect(action: "show", id:  role.id, fragment:"tab-members")
  }

  def removemember() {
    def role = Role.get(params.id)

    def subject = Subject.get(params.subjectID)
    if (!subject) {
      log.warn "No subject for $params.subjectID located when attempting to removemember"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.role.removemember.nosubject'

      redirect(action: "list")
      return
    }

    roleService.deleteMember(subject, role)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.role.removemember.success'
    redirect(action: "show", id:  role.id, fragment:"tab-members")
  }

  def createpermission() {
    def role = Role.get(params.id)

    def permission = new Permission()
    permission.type = Permission.defaultPerm
    permission.target = params.target
    permission.owner = role
    
    permissionService.createPermission(permission, role)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.role.createpermission.success'

    redirect(action: "show", id:  role.id, fragment:"tab-permissions")
  }

  def deletepermission() {
    def role = Role.get(params.id)

    def permission = Permission.get(params.permID)
    if (!permission) {
      log.warn "No permission for $params.permID located when attempting to deletepermission"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.role.deletepermission.nopermission'

      redirect(action: "list")
      return
    }

    permissionService.deletePermission(permission)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.role.deletepermission.success'
    redirect(action: "show", id:  role.id, fragment:"tab-permissions")
  }

  def finalization() {
    def roleInvitation = RoleInvitation.findWhere(inviteCode:params.inviteCode)
    if(!roleInvitation) {
      log.error "No invitation for ${params.inviteCode}"

      redirect action: "finalizationerror"
      return
    }

    if(roleService.finalizeInvitation(roleInvitation)) {
      log.info "$subject successfully claimed $roleInvitation redirecting to ${roleInvitation.redirectTo}"

      flash.type = 'success'
      flash.message = 'controllers.aaf.base.admin.role.finalization.success'

      redirect url: roleInvitation.redirectTo
    } else {
      log.info "$subject presented invalid invitation"
      redirect action:'finalizationerror'
    }
  }

  def finalizationerror() {
  }

  private validRole() {
    if(!params.id) {
      log.warn "Role ID was not present"

      flash.type = 'info'
      flash.message = 'controllers.aaf.base.admin.role.noroleid'

      redirect action:'list'
      return false
    }

    def role = Role.get(params.id)
    if (!role) {
      log.warn "No role for ${params.id} located"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.role.nonexistant'

      redirect(action: "list")
      return false
    }
  }
}
