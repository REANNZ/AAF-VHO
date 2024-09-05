<%=packageName ? "package ${packageName}\n\n" : ''%>import org.springframework.dao.DataIntegrityViolationException
import org.apache.shiro.SecurityUtils

class ${className}Controller {

  static defaultAction = "list"
  static allowedMethods = [save: "POST", update: "POST", delete: "DELETE"]

  def beforeInterceptor = [action: this.&valid${className}, except: ['list', 'create', 'save']]

  def list() {
    log.info "Action: list, Subject: \$subject"
    [${propertyName}List: ${className}.list(params), ${propertyName}Total: ${className}.count()]
  }

  def show(Long id) {
    log.info "Action: show, Subject: \$subject"
    def ${propertyName} = ${className}.get(id)
    [${propertyName}: ${propertyName}]
  }

  def create() {
    if(SecurityUtils.subject.isPermitted("app:manage:${className.toLowerCase()}:create")) {
      log.info "Action: create, Subject: \$subject"
      [${propertyName}: new ${className}(params)]
    }
    else {
      log.warn "Attempt to do administrative ${className} create by \$subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def save() {
    if(SecurityUtils.subject.isPermitted("app:manage:${className.toLowerCase()}:create")) {
      def ${propertyName} = new ${className}(params)
      if (!${propertyName}.save()) {
        flash.type = 'error'
        flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.save.failed'
        render(view: "create", model: [${propertyName}: ${propertyName}])
        return
      }

      log.info "Action: save, Subject: \$subject, Object: \${${propertyName}}"
      flash.type = 'success'
      flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.save.success'
      redirect(action: "show", id: ${propertyName}.id)
    }
    else {
      log.warn "Attempt to do administrative ${className} save by \$subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def edit(Long id) {
    if(SecurityUtils.subject.isPermitted("app:manage:${className.toLowerCase()}:\$id:edit")) {
      log.info "Action: edit, Subject: \$subject, Object: ${propertyName}"
      def ${propertyName} = ${className}.get(id)
      [${propertyName}: ${propertyName}]
    }
    else {
      log.warn "Attempt to do administrative ${className} edit by \$subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def update(Long id, Long version) {
    if(SecurityUtils.subject.isPermitted("app:manage:${className.toLowerCase()}:\$id:edit")) {
      def ${propertyName} = ${className}.get(id)
      
      if (version == null) {
        flash.type = 'error'
        flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.noversion'
        render(view: "edit", model: [${propertyName}: ${propertyName}])
        return
      }

      if (${propertyName}.version > version) {<% def lowerCaseName = grails.util.GrailsNameUtils.getPropertyName(className) %>
        ${propertyName}.errors.rejectValue("version", "controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.optimistic.locking.failure")
        render(view: "edit", model: [${propertyName}: ${propertyName}])
        return
      }

      ${propertyName}.properties = params

      if (!${propertyName}.save()) {
        flash.type = 'error'
        flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.failed'
        render(view: "edit", model: [${propertyName}: ${propertyName}])
        return
      }

      log.info "Action: update, Subject: \$subject, Object: \${${propertyName}}"
      flash.type = 'success'
      flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.update.success'
      redirect(action: "show", id: ${propertyName}.id)
    }
    else {
      log.warn "Attempt to do administrative ${className} update by \$subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  def delete(Long id) {
    if(SecurityUtils.subject.isPermitted("app:manage:${className.toLowerCase()}:\$id:delete")) {
      def ${propertyName} = ${className}.get(id)
      try {
        ${propertyName}.delete()

        log.info "Action: delete, Subject: \$subject, Object: ${propertyName}"
        flash.type = 'success'
        flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.delete.success'
        redirect(action: "list")
      }
      catch (DataIntegrityViolationException e) {
        flash.type = 'error'
        flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.delete.failure'
        redirect(action: "show", id: id)
      }
    }
    else {
      log.warn "Attempt to do administrative ${className} delete by \$subject was denied - not permitted by assigned permissions"
      response.sendError 403
    }
  }

  private valid${className}() {
    if(!params.id) {
      log.warn "ID was not present"

      flash.type = 'info'
      flash.message = message(code: 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.no.id')

      redirect action:'list'
      return false
    }

    def ${propertyName} = ${className}.get(params.id)
    if (!${propertyName}) {
      log.warn "${propertyName} was not a valid instance"

      flash.type = 'info'
      flash.message = 'controllers.${packageName.toLowerCase()}.${className.toLowerCase()}.notfound'

      redirect action:'list'
      return false
    }
  }
}
