package aaf.base.admin

class EmailTemplateController {
  def defaultAction = "list"

  def beforeInterceptor = [action: this.&validEmailTemplate, except: ['list', 'create', 'save']]

  def list = {
    def emailtemplateList = EmailTemplate.getAll()
    [emailtemplateList: emailtemplateList]
  }
  
  def create = {
    def emailtemplate = new EmailTemplate()
    [emailtemplate: emailtemplate]
  }
  
  def save = {    
    def emailtemplate = new EmailTemplate(params)
    if(!emailtemplate.save()) {

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.emailtemplate.save.failure'

      render view: "create", model: [emailtemplate: emailtemplate]
      return
    }
    
    log.info "$subject created $emailtemplate"

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.emailtemplate.save.success'
    redirect action: "show", id: emailtemplate.id
  }

  def show = {
    def emailtemplate = EmailTemplate.get(params.id)
    [emailtemplate:emailtemplate]
  }

  def edit = {
    def emailtemplate = EmailTemplate.get(params.id)
    [emailtemplate:emailtemplate]
  }

  def update = {
    def emailtemplate = EmailTemplate.get(params.id)

    emailtemplate.properties = params
    emailtemplate.save()
    if(emailtemplate.hasErrors()) {
      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.emailtemplate.update.failure'

      render view: "edit", model: [emailtemplate: emailtemplate]
      return
    }

    log.info "$subject updated $emailtemplate"

    flash.type = 'success'
    flash.message = 'controllers.aaf.base.admin.emailtemplate.update.success'

    redirect action: "show", id: emailtemplate.id
  }

 private validEmailTemplate() {
    if(!params.id) {
      log.warn "EmailTemplate ID was not present"

      flash.type = 'info'
      flash.message = message(code: 'controllers.aaf.base.admin.emailtemplate.noemailtemplateid')

      redirect action:'list'
      return false
    }

    def emailtemplate = EmailTemplate.get(params.id)
    if(!emailtemplate) {

      log.warn "EmailTemplate identified by ${params.id} does not exist"

      flash.type = 'error'
      flash.message = 'controllers.aaf.base.admin.emailtemplate.nonexistant'

      redirect action:'list'
      return false
    }
  }
}
