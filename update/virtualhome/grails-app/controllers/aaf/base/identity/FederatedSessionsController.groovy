package aaf.base.identity

import org.apache.shiro.SecurityUtils
import org.apache.shiro.authc.AuthenticationException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.IncorrectCredentialsException

class FederatedSessionsController {    
  def grailsApplication
  
  def federatedlogin = {
    def incomplete = false
    def errors = []

    if (!grailsApplication.config.aaf.base.realms.federated.active) {
      log.error("Attempt to do federated login when Apache SP is not marked active in local configuration")
      response.sendError(403)
      return
    }

    def ua = request.getHeader("User-Agent")
    if (!ua) {
      incomplete = true
      errors.add "Browser User Agent was not presented"
    }
    
    def principal = federatedAttributeValue(grailsApplication, grailsApplication.config.aaf.base.realms.federated.mapping.principal)
    def credential = federatedAttributeValue(grailsApplication, grailsApplication.config.aaf.base.realms.federated.mapping.credential)
    def sharedToken = federatedAttributeValue(grailsApplication, grailsApplication.config.aaf.base.realms.federated.mapping.sharedToken)
    
    def attributes = [:]  
    attributes.entityID = federatedAttributeValue(grailsApplication, grailsApplication.config.aaf.base.realms.federated.mapping.entityID)
    attributes.cn =  federatedAttributeValue(grailsApplication, grailsApplication.config.aaf.base.realms.federated.mapping.cn)
    attributes.email = federatedAttributeValue(grailsApplication, grailsApplication.config.aaf.base.realms.federated.mapping.email)
    
    if (!principal) {
      incomplete = true
      errors.add "Your unique account identifier (persistent-id, urn:oid:1.3.6.1.4.1.5923.1.1.1.10) was unable to be obtained from the provided assertion"
    }

    if (!credential) {
      incomplete = true
      errors.add "An internal SAML session identifier (Shib-Session-ID) was unable to be obtained from the provided assertion"
    }
    if (!attributes.entityID) {
      incomplete = true
      errors.add "An EntityID was unable to be obtained from the provided assertion"
    }

    if (grailsApplication.config.aaf.base.realms.federated.require.sharedtoken) {
      if (!sharedToken) {
        incomplete = true
        errors.add "Your identifier (auEduPersonSharedToken) was unable to be obtained from the provided assertion"
      }
    }

    if (grailsApplication.config.aaf.base.realms.federated.require.cn) {
      if (!attributes.cn) {
        incomplete = true
        errors.add "Your common name (cn, urn:oid:2.5.4.3) was unable to be obtained from the provided assertion"
      }
    }

    if (grailsApplication.config.aaf.base.realms.federated.require.email) {
      if (!attributes.email) {
        incomplete = true
        errors.add "Your email address (mail, urn:oid:0.9.2342.19200300.100.1.3) was unable to be obtained from the provided assertion"
      }
    }
    
    if(incomplete) {
      log.warn "Incomplete federated authentication attempt was aborted"
      errors.each { log.warn it }
      render view:'federatedincomplete', model:[errors:errors]
      return
    }
    
    try {
      def remoteHost = request.getRemoteHost()
      if (ua.length() > 254) // Handle stupid user agents that present every detail known to man about corporate environments
        ua = ua.substring(0,254) 
      
      def token = new FederatedToken(principal:principal, credential:credential, sharedToken:sharedToken, attributes:attributes, remoteHost:remoteHost, userAgent:ua ) 
      
      log.info "Attempting federation based authentication event for subject identified in $token"
      SecurityUtils.subject.login(token)
      
      log.info "Successfully processed federation based authentication event for subject $principal based on credential provided in $credential, redirecting to content"
      def targetUri = session.getAttribute(AuthController.TARGET)
      session.removeAttribute(AuthController.TARGET)
      targetUri ? redirect(uri: targetUri) : redirect(uri:"/")
      return
    }
    catch (IncorrectCredentialsException e) {
      log.warn "Federated credentials failure for subject $principal, incorrect credentials."
      log.debug e
    }
    catch (DisabledAccountException e) {
      log.warn "Federated credentials failure for subject $principal"
      log.debug e
    }
    catch (AuthenticationException e) {
      log.warn "Federated credentials failure for subject $principal, generic fault"
      log.debug e
    }

    redirect controller: 'auth', action: 'federatederror'
  }

  def federatedincomplete = {}
  
  private String federatedAttributeValue(def grailsApplication, String attr) {
    def value = null
    if(grailsApplication.config.aaf.base.realms.federated.request.attributes) {
      if(request.getAttribute(attr))
        value = new String(request.getAttribute(attr).getBytes("ISO-8859-1"))
    } else {
      if(request.getHeader(attr))
        value = new String(request.getHeader(attr).getBytes("ISO-8859-1"))  // Not as secure
    }
    
    value
  }
}
