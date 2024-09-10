package aaf.base.workflow

class WorkflowProcessService {
  
  def workflowTaskService
  
  def create(def definition) {
    Binding binding = new Binding()
    binding.process = { m, closure ->
      this.interpret(m, definition, closure)
    }
    
    def script = new GroovyShell().parse(definition)
    script.binding = binding
    def (created, process) = script.run()
    
    [created, process]
  }
  
  def update(String processName, def definition) {
    def process = Process.findWhere(name: processName, active: true)
    if (!process) {
      log.error "Unable to update process ${processName}"
      log.error "The process definition for ${processName} was not found, no such process by name or no process definition is active"
      return [false, null]
    }
    
    Binding binding = new Binding()
    binding.process = { m, closure ->
      this.interpret(m, definition, closure)
    }
    
    def script = new GroovyShell().parse(definition)
    script.binding = binding
    def (created, newProcess) = script.run()
    
    process.active = false
    if(!process.save()) {
      log.error "Unable to update ${process} with false active state"
      process.errors.each {
        log.error it
      }
      throw new ErronousWorkflowStateException("Unable to save when applying update to ${process}")
    }
    
    [created, newProcess]
  }
  
  def interpret(Map m, String definition, Closure closure) {
    def processes = Process.findAllByName(m.name)
    int processVersion = 1
    if (processes) {
      processVersion = processes.size() + 1
    }

    def process = new Process(name: m.name, description: m.description, processVersion: processVersion, creator: subject, definition: definition, active:true)
    closure.delegate = new ProcessDelegate(process)
    closure()
    
    if(!process.validate()) {
      process.errors.each {
        log.error it
      }
      return [false, process]
    }
    
    if(!process.save()) {
      process.errors.each {
        log.error it
      }
      throw new ErronousWorkflowStateException("Unable to save when interpreting ${process}")
    }
    
    processes.each {
      if(it.active) {
        it.active = false;
        if(!it.save())
        throw new ErronousWorkflowStateException("Unable to save when deactivating ${process}")
      }
    }
    
    [true, process]
  }
  
  def initiate(String processName, String instanceDescription, ProcessPriority priority, Map params) {
    if (params.agent == null && (params.agentCN == null || params.agentEmail == null)) {
      log.error "Unable to initiate an instance of process ${processName}"
      log.error "The params map must contain a valid aaf.base.identity.Subject object with the key 'agent' when the processInstance is being created for a known subject OR it must contain the keys 'agentCN' and 'agentEmail' if the processInstance is being created for a public entity."
      return [false, null]
    }

    def process = Process.findWhere(name: processName, active: true)
    if (!process) {
      log.error "Unable to initiate an instance of process ${processName}"
      log.error "The process definition for ${processName} was not found, no such process by name or no process definition is active"
      return [false, null]
    }

    
    ProcessInstance processInstance = new ProcessInstance(process: process, description: instanceDescription, status: ProcessStatus.INPROGRESS, priority: priority ?:ProcessPriority.LOW)
    if (params.agent) {
      processInstance.agent = params.agent
      params.agent = params.agent.principal   // We can only store strings in the params map so we change it here to be principal instead of the object.
    } else {
      processInstance.agentCN = params.agentCN
      processInstance.agentEmail = params.agentEmail
    }

    processInstance.params = params
    process.addToInstances(processInstance)
    
    if(!process.save()) {
      log.error "Unable to initiate an instance of process ${processName}"
      process.errors.each {
        log.error it
      }
      throw new ErronousWorkflowStateException("Unable to save ${process} when initiating instance for ${instanceDescription}")
    }
    
    def processInstance_ = processInstance.save()
    if(!processInstance_) {
      log.error "Unable to initiate an instance of process ${processName}"
      processInstance.errors.each {
        log.error it
      }
      throw new ErronousWorkflowStateException("Unable to save ${processInstance} when initiating instance of ${process}")
    }
    
    [true, processInstance_]
  }
  
  def run(ProcessInstance processInstance) {
    workflowTaskService.initiate(processInstance.id, processInstance.process.tasks.get(0).id)
  }
  
}
