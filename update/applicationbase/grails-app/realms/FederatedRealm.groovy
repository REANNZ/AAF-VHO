
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.IncorrectCredentialsException

import aaf.base.identity.*

class FederatedRealm {
  static authTokenClass = aaf.base.identity.FederatedToken
  
  def grailsApplication
  def roleService
  def permissionService
  
  def authenticate(token) {
    if (!grailsApplication.config.aaf.base.realms.federated.active) {
      log.error "Authentication attempt for federated provider, denying attempt as federation integration disabled"
      throw new UnknownAccountException ("Authentication attempt for federated provider, denying attempt as federation disabled")
    }
    if (!token.principal) {
      log.error "Authentication attempt for federated provider, denying attempt as no persistent identifier was provided"
      throw new UnknownAccountException("Authentication attempt for federated provider, denying attempt as no persistent identifier was provided")
    }
    if (!token.credential) {
      log.error "Authentication attempt for federated provider, denying attempt as no credential was provided"
      throw new UnknownAccountException("Authentication attempt for federated provider, denying attempt as no credential was provided")
    }
    if (grailsApplication.config.aaf.base.realms.federated.require.sharedtoken) {
      if (!token.sharedToken) {
        log.error "Authentication attempt for federated provider, denying attempt as no shared token was provided"
        throw new UnknownAccountException("Authentication attempt for federated provider, denying attempt as no shared token was provided")
      }
    }
    if (grailsApplication.config.aaf.base.realms.federated.require.cn) {
      if (!token.attributes?.cn) {
        log.error "Authentication attempt for federated provider, denying attempt as no cn was provided"
        throw new UnknownAccountException("Authentication attempt for federated provider, denying attempt as no cn was provided")
      }
    }
    if (grailsApplication.config.aaf.base.realms.federated.require.email) {
      if (!token.attributes?.email) {
        log.error "Authentication attempt for federated provider, denying attempt as no email was provided"
        throw new UnknownAccountException("Authentication attempt for federated provider, denying attempt as no email was provided")
      }
    }
    Subject.withTransaction {
      Subject subject = Subject.findByPrincipal(token.principal)

      if(!subject && token.sharedToken)  // Failover to lookup by SharedToken if available
        subject = Subject.findBySharedToken(token.sharedToken)

      if (!subject) {
        if(!grailsApplication.config.aaf.base.realms.federated.auto_provision) {
          log.error "Authentication attempt for federated provider, denying attempt as federation integration is denying automated account provisioning"
          throw new DisabledAccountException("Authentication attempt for federated provider, denying attempt as federation integration is denying automated account provisioning")
        }
        
        // Here we don't already have a subject stored in the system so we need to create one
        log.info "No subject represented by ${token.principal} exists in local repository, provisioning new account"
        
        subject = new Subject()
        subject.principal = token.principal
        subject.enabled = true
        
        subject.cn = token.attributes?.cn
        if(token.attributes?.email?.contains(';')) {
          log.warn "Email provided for ${token.principal} is multivalued (${token.attributes.email}) attempting to split on ; and use first returned value."
          subject.email = token.attributes?.email?.toLowerCase().split(';')[0]
        } else {
          subject.email = token.attributes?.email?.toLowerCase()
        }
        subject.sharedToken = token.sharedToken
        
        // Store in data repository
        if(!subject.save()) {
          subject.errors.each { err ->
            log.error err
          }
          throw new RuntimeException("Account creation exception for new federated account for ${token.principal}")
        }

        log.info("Created ${subject} from federated attribute statement")
      } else {
        subject.principal = token.principal   // Ensure we catch EPTID change linked by the same SharedToken value
        subject.cn = token.attributes.cn
        
        if(token.attributes?.email?.contains(';')) {
          log.warn "Email provided for ${token.principal} is multivalued (${token.attributes.email}) attempting to split on ; and use first returned value."
          subject.email = token.attributes?.email?.toLowerCase().split(';')[0]
        } else {
          subject.email = token.attributes?.email?.toLowerCase()
        }

        // If we start recieving ST for an existing account then store it
        if(subject.sharedToken == null && token.sharedToken) {
          subject.sharedToken = token.sharedToken
        }

        if (subject.sharedToken != token.sharedToken) {
          log.error("Authentication halted for ${subject} as current sharedToken ${subject.sharedToken} does not match incoming token ${token.sharedToken}")
          throw new IncorrectCredentialsException("${subject} authentication halted as current sharedToken ${subject.sharedToken} does not match incoming token ${token.sharedToken}")
        }

        // Store in data repository
        if(!subject.save()) {
          subject.errors.each { err ->
            log.error err
          }
          throw new RuntimeException("Account update exception for existing federated account ${token.principal}")
        }
        log.info("Updated ${subject} from federated attribute statement")
      }
      
      if (!subject.enabled) {
        log.warn("Attempt to authenticate using using federated principal mapped to a locally disabled account [${subject.id}]${subject.principal}")
        throw new DisabledAccountException("Attempt to authenticate using using federated principal mapped to a locally disabled account [${subject.id}]${subject.principal}")
      }
      
      // Security context is now successfully established
      def sessionRecord = new SessionRecord(credential:token.credential, remoteHost:token.remoteHost, userAgent:token.userAgent)
      subject.addToSessionRecords(sessionRecord)
      if(!subject.save()) {
        subject.errors.each { err ->
          log.error err
        }
        throw new RuntimeException("Account modification failed for ${token.principal} when adding new session record")
      }
      
      log.info "Successfully logged in subject [$subject.id]$subject.principal using federated source"
      def account = new SimpleAccount(subject.id, token.credential, "aaf.base.identity.FederatedToken")
      return account
    }
  }
}
