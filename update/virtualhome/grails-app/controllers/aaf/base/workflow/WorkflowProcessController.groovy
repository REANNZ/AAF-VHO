package aaf.base.workflow

class WorkflowProcessController {
  static defaultAction = "list"
  
  def beforeInterceptor = [action: this.&validProcess, except: ['list', 'create', 'save']]

  def workflowProcessService

  def list = {
    def processList = Process.findAllWhere(active: true)
    [processList: processList]
  }
  
  def create = {
    def process = new Process()
    [process: process]
  }
  
  def save = {
    if(!params.code) {
      log.warn "Process definition was not present"
      flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.save.nocode')
      render view: "create"
      return
    }
    
    def created, process
    try {
      (created, process) = workflowProcessService.create(params.code)
      if(!created) {
        process.errors.each {
          log.debug it
        }
        flash.type = "error"
        flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.save.notcreated')
        render view: "create", model: [process: process]
        return
      }
      
      flash.type = "success"
      flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.save.success')
      log.info "$subject created $process"
      redirect action: "show", id: process.id
    }
    catch(Exception e) {
      log.error e
      
      process = new Process(definition: params.code)
      flash.type = "error"
      flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.save.totalfailure')
      render view: "create", model: [process: process]
    }
  }

  def show = {
    def process = Process.get(params.id)    
    [process:process]
  }
  
  def edit = {
    def process = Process.get(params.id)
    [process:process]
  }
  
  def update = {    
    def process = Process.get(params.id)

    def updated, process_
    try {
      log.info "$subject is updating $process"
      (updated, process_) = workflowProcessService.update(process.name, params.code)
      
      if(!updated) {
        process_.errors.each {
          log.debug it
        }
        flash.type = "error"
        flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.update.error')
        process_.id = process.id
        render view: "edit", model: [process: process_]
        return
      }
      
      log.info "$subject updated $process_"
      flash.type = "success"
      flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.update.success')
      redirect action: "show", id: process_.id
      
    }
    catch(Exception e) {
      flash.type = "error"
      flash.message = message(code: 'controllers.aaf.base.workflow.workflowprocess.update.totalfailure')
      render view: "edit", model: [process: process]
    }
  }

  private validProcess() {
    if(!params.id) {
      log.warn "Process ID was not present"

      flash.type = 'info'
      flash.message = 'controllers.aaf.base.workflow.workflowprocess.noprocessid'

      redirect action:'list'
      return false
    }

    def process = Process.get(params.id)
    if(!process) {

      log.warn "Process identified by ${params.id} does not exist"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.workflow.workflowprocess.nonexistant'

      redirect action:'list'
      return false
    }
  }
}
