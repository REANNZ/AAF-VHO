package aaf.base.workflow

class WorkflowScriptController {
  def defaultAction = "list"

  def list = {
    def scriptList = WorkflowScript.getAll()
    [scriptList: scriptList]
  }
  
  def create = {
    def script = new WorkflowScript()
    [script: script]
  }
  
  def save = {
    if(!params.definition) {
      log.warn "Script definition was not present"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.workflow.workflowscript.save.nodefinition'

      redirect action:list
      return
    }
    
    def script = new WorkflowScript(params)
    script.creator = subject
    if(!script.save()) {

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.workflow.workflowscript.save.failure'

      render view: "create", model: [script: script]
      return
    }
    
    log.info "$subject created $script"

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.workflow.workflowscript.save.success'
    redirect action: "show", id: script.id
  }

  def show = {
    def script = WorkflowScript.get(params.id)
    [script:script]
  }

  def edit = {
    def script = WorkflowScript.get(params.id)
    [script:script]
  }

  def update = {
    def script = WorkflowScript.get(params.id)

    script.properties = params
    script.save()
    if(script.hasErrors()) {
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.workflow.workflowscript.update.failure'

      render view: "edit", model: [script: script]
      return
    }

    log.info "$subject updated $script"

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.workflow.workflowscript.update.success'

    redirect action: "show", id: script.id
  }
}
