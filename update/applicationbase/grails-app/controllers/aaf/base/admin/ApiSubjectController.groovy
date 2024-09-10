package aaf.base.admin

import aaf.base.identity.*

class ApiSubjectController {
  static defaultAction = "list"
  static allowedMethods = [save: "POST", update:"POST", delete:"DELETE", enablesubject: "POST", disablesubject: "POST", createpermission: "POST", deletepermission: "POST"]

  def permissionService

  def beforeInterceptor = [action: this.&validSubject, except: ['index', 'list', 'create', 'save']]

  // TODO: This controller requires test cases to be built

  def index() {
    redirect(action: "list", params: params)
  }
  
  def list() {
    [apiSubjects:ApiSubject.list()]
  }

  def show() {
    def apiSubject = ApiSubject.get(params.id)
    [apiSubject: apiSubject]
  }

  def create() {
    def apiSubject = new ApiSubject()
    [apiSubject: apiSubject]
  }

  def save() {
    def apiSubject = new ApiSubject(enabled:true)
    bindData(apiSubject, params, [include:['principal', 'apiKey', 'email', 'description']])
    if(apiSubject.save()) {
      flash.type = 'success'
      flash.message = 'controllers.aaf.base.admin.apisubject.create.success'
      redirect(action: "show", id: apiSubject.id)
    }
    else {
      apiSubject.errors.each {
        log.warn it
      }
      render view:'create', model:[apiSubject:apiSubject]
    }
  }

  def edit() {
    def apiSubject = Subject.get(params.id)
    [apiSubject:apiSubject]
  }

  def update() {
    def apiSubject = Subject.get(params.id)
    bindData(apiSubject, params, [include:['principal', 'apiKey', 'email', 'description']])
    if(apiSubject.save()) {
      flash.type = 'success'
      flash.message = 'controllers.aaf.base.admin.apisubject.update.success'
      redirect(action: "show", id: apiSubject.id)
    }
    else {
      apiSubject.errors.each {
        log.warn it
      }
      render view:'edit', model:[apiSubject:apiSubject]
    }
  }

  def delete() {
    def apiSubject = Subject.get(params.id)

    apiSubject.delete()

    if(apiSubject.hasErrors()) {
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.apisubject.delete.error'
      redirect(action: "show", id: apiSubject.id)
      return
    }

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.apisubject.delete.success'

    redirect(action: "list")
  }

  def enablesubject() {
    def apiSubject = Subject.get(params.id)
    apiSubject.enabled = true

    if(!apiSubject.save()) {
      response.sendError(500, "Error setting $apiSubject to enabled state, object persistance failed") 
      return
    }

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.apisubject.enabled'
    redirect(action: "show", id:  apiSubject.id)
  }

  def disablesubject() {
    def apiSubject = ApiSubject.get(params.id)

    apiSubject.enabled = false
    if(!apiSubject.save()) {
      response.sendError(500, "Error setting $apiSubject to disabled state, object persistance failed") 
      return
    }

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.apisubject.disabled'
    redirect(action: "show", id:  apiSubject.id)
  }

  def createpermission() {
    def apiSubject = ApiSubject.get(params.id)

    if (!params.target) {
      log.warn "No permission target located when trying to add new permission to $apiSubject"
      
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.apisubject.create.permission.nodata'

      redirect(action: "show", id:  apiSubject.id, fragment:"tab-permissions")
      return
    }

    def permission = new Permission()
    permission.type = Permission.defaultPerm
    permission.target = params.target
    permission.owner = apiSubject
    
    permissionService.createPermission(permission, apiSubject) 

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.apisubject.create.permission.success'
    redirect(action: "show", id: apiSubject.id, fragment:"tab-permissions")
  }

  def deletepermission() {
    def apiSubject = ApiSubject.get(params.id)
    def permission = Permission.get(params.permID)
    if (!permission) {
      log.warn "No permission for $params.permID located when attempting to deletepermission"
      
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.apisubject.delete.permission.nonexistant'

      redirect(action: "show", id: apiSubject.id, fragment:"tab-permissions")
      return
    }

    permissionService.deletePermission(permission)

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.apisubject.delete.permission.success'
    redirect(action: "show", id: apiSubject.id, fragment:"tab-permissions")
  }

  private validSubject() {
    if(!params.id) {
      log.warn "Subject ID was not present"

      flash.type = 'info'
      flash.message = 'controllers.aaf.base.identity.apisubject.nosubjectid'

      redirect action:'list'
      return false
    }

    def apiSubject = ApiSubject.get(params.id)
    if (!apiSubject) {
      log.warn "No apiSubject for $params.id located"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.identity.apisubject.nonexistant'

      redirect(action: "list")
      return false
    }
  }

}
