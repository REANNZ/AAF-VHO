package aaf.base.admin

import aaf.base.identity.*

class SubjectController {
  static defaultAction = "list"
  static allowedMethods = [enablesubject: "POST", disablesubject: "POST", createpermission: "POST", deletepermission: "POST"]

  def permissionService

  def beforeInterceptor = [action: this.&validSubject, except: ['index', 'list']]

  def index() {
    redirect(action: "list", params: params)
  }
  
  def list() {
    [subjects:Subject.list()]
  }

  def show() {
    def subject = Subject.get(params.id)
    [subject: subject]
  }

  def showpublic() {
    def subject = Subject.get(params.id)
    [subject: subject]
  }

  def enablesubject() {
    def subject = Subject.get(params.id)
    subject.enabled = true

    if(!subject.save()) {
      response.sendError(500, "Error setting $subject to enabled state, object persistance failed") 
      return
    }

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.subject.enabled'
    redirect(action: "show", id:  subject.id)
  }

  def disablesubject() {
    def subject = Subject.get(params.id)

    subject.enabled = false
    if(!subject.save()) {
      response.sendError(500, "Error setting $subject to disabled state, object persistance failed") 
      return
    }

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.subject.disabled'
    redirect(action: "show", id:  subject.id)
  }

  def createpermission() {
    def subject = Subject.get(params.id)

    if (!params.target) {
      log.warn "No permission target located when trying to add new permission to $subject"
      
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.subject.create.permission.nodata'

      redirect(action: "show", id:  subject.id, fragment:"tab-permissions")
      return
    }

    def permission = new Permission()
    permission.type = Permission.defaultPerm
    permission.target = params.target
    permission.owner = subject
    
    permissionService.createPermission(permission, subject) 

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.subject.create.permission.success'
    redirect(action: "show", id:  subject.id, fragment:"tab-permissions")
  }

  def deletepermission() {
    def subject = Subject.get(params.id)
    def permission = Permission.get(params.permID)
    if (!permission) {
      log.warn "No permission for $params.permID located when attempting to deletepermission"
      
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.subject.delete.permission.nonexistant'

      redirect(action: "show", id: subject.id, fragment:"tab-permissions")
      return
    }

    permissionService.deletePermission(permission)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.subject.delete.permission.success'
    redirect(action: "show", id: subject.id, fragment: "tab-permissions")
  }

  private validSubject() {
    if(!params.id) {
      log.warn "Subject ID was not present"

      flash.type = 'info'
      flash.message = 'controllers.aaf.base.identity.subject.nosubjectid'

      redirect action:'list'
      return false
    }

    def subject = Subject.get(params.id)
    if (!subject) {
      log.warn "No subject for $params.id located"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.identity.subject.nonexistant'

      redirect(action: "list")
      return false
    }
  }

}
