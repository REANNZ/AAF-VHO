package aaf.base.identity

import org.apache.shiro.SecurityUtils

class AuthController {
  static final String TARGET = 'grails.controllers.aaf.base.identity.AuthControllerController:TARGETURI'

  static defaultAction = "login"
    
  def grailsApplication

  def login = {
    // Stores initial content user is attempting to access to redirect when auth complete
    if(params.targetUri)
      session.setAttribute(AuthController.TARGET, params.targetUri)

    // Integrates with Shibboleth NativeSPSessionCreationParameters as per https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPSessionCreationParameters
    // This allows us to mix and match publicly available and private content within by making use of security filters in conf/SecurityFilters.groovy
    def localAction = createLink([controller:'federatedSessions', action: 'federatedlogin', absolute: true])
    def url = "${grailsApplication.config.aaf.base.realms.federated.sso_endpoint}?target=${localAction}"

    // If this a production scenario we defer to shibboleth
    if (grailsApplication.config.aaf.base.realms.federated.automate_login) {
      redirect (url: url)
      return
    }

    [spsession_url: url]
  }
  
  def logout = {
    if(subject)
      log.info("Signing out subject [${subject?.id}] ${subject?.cn} - ${subject?.email}")
    else
      log.info("Signing out session with no active subject")
    
    SecurityUtils.subject?.logout()
    session.invalidate()

    if(params.targetUri)
      redirect(uri: params.targetUri)
    else
      redirect(controller:'dashboard', action:'welcome')
  }

  def poll = {
    render "Polled webserver successfully"
  }
  
  def federatederror = {}

  def echo = {
    def attr = [:]
    if(grailsApplication.config.aaf.base.realms.federated.request.attributes) {
      request.attributeNames.each {
        attr.put(it, (String)request.getAttribute(it))
      }
    } else {
      request.headerNames.each {
        attr.put(it, (String)request.getHeader(it))
      } 
    }
    return [attr: attr]
  }

}
