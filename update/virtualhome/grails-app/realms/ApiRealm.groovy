
import org.apache.shiro.authc.UnknownAccountException
import org.apache.shiro.authc.DisabledAccountException
import org.apache.shiro.authc.SimpleAccount
import org.apache.shiro.authc.IncorrectCredentialsException

import java.text.*

import aaf.base.identity.*

import java.security.MessageDigest
import javax.crypto.spec.SecretKeySpec
import javax.crypto.Mac

class ApiRealm {

  static authTokenClass = aaf.base.identity.ApiToken

  final def authorizeRegex = ~/AAF-HMAC-SHA256 token="([^"]+)", signature="([^"]*)?"/
  final String dateFormat = "EEE, d MMM yyyy HH:mm:ss z";
  
  def grailsApplication

  def authenticate(token) {
    if (!grailsApplication.config.aaf.base.realms.api.active) {
      log.error "Authentication attempt via API. Denying attempt as authenticated api integration disabled."
      throw new UnknownAccountException ("Authentication attempt via API. Denying attempt as authenticated api integration disabled.")
    }

    // Ensure valid timestamp for request
    // All HTTP date/time stamps MUST be represented in Greenwich Mean Time (GMT), without exception
    def dateHeader = token.request.getHeader('Date') ?:token.request.getHeader('X-AAF-Date')
    if(!dateHeader) {
      log.error "[Requester: ${token.request.remoteHost}] - Authentication halted as Date header not provided."
      throw new IncorrectCredentialsException("[Requester: ${token.request.remoteHost}] - Authentication halted as Date header not provided.")
    }
    try {
      log.info "[Requester: ${token.request.remoteHost}] - Validating Date header for API request with value of $dateHeader"
      SimpleDateFormat sdf = new SimpleDateFormat(dateFormat)

      def date = sdf.parse(dateHeader)
      if(!validateRequestDate(date.time)) {
        log.error "[Requester: ${token.request.remoteHost}] - Authentication halted as Date header shows request time outside allowed range."
        throw new IncorrectCredentialsException("[Requester: ${token.request.remoteHost}] - Authentication halted as Date header shows request time outside allowed range.")
      } else {
        log.info "[Requester: ${token.request.remoteHost}] - Date of request within valid range"
      }

    } 
    catch (ParseException e)
    {
      log.error "[Requester: ${token.request.remoteHost}] - Authentication halted as Date header provided in invalid format."
      throw new IncorrectCredentialsException("[Requester: ${token.request.remoteHost}] - Authentication halted as Date header provided in invalid format.")
    }

    // Authorize header in format:
    // Authorize: AAF-HMAC-SHA256 token="...", signature="..."
    def authorize = token.request.getHeader('Authorization')
    if(!authorize || !(authorize ==~ authorizeRegex)) {
      log.error "[Requester: ${token.request.remoteHost}] - Authentication halted as Authorize header provides invalid data for AAF-HMAC-SHA256 auth-scheme."
      throw new IncorrectCredentialsException("[Requester: ${token.request.remoteHost}] - Authentication halted as Authorize header provides invalid data for AAF-HMAC-SHA256 auth-scheme.")
    }

    def authorizeTokens = authorize =~ authorizeRegex
    def principal = authorizeTokens[0][1]
    def signature = authorizeTokens[0][2]

    ApiSubject apiSubjectInstance = ApiSubject.findByPrincipal(principal)
    if(!apiSubjectInstance) {
      log.error("[Requester: ${token.request.remoteHost}] - Authentication halted as API Subject principal value is invalid")
      throw new UnknownAccountException("[Requester: ${token.request.remoteHost}] - Authentication halted as API Subject principal value is invalid")
    }

    if(!apiSubjectInstance.enabled) {
      log.error("[Requester: ${token.request.remoteHost}] - Authentication halted as account disabled")
      throw new DisabledAccountException("[Requester: ${token.request.remoteHost}] - Authentication halted as account disabled")
    }

    if(!validateSignature(token.request, signature, apiSubjectInstance)) {
      log.error("[Requester: ${token.request.remoteHost}] - Authentication halted calculated signature is invalid.")
      throw new IncorrectCredentialsException("[Requester: ${token.request.remoteHost}] - Authentication halted as calculated signature is invalid.")
    }

    def account = new SimpleAccount(apiSubjectInstance.id, apiSubjectInstance.principal, "aaf.base.identity.ApiToken")

    log.info "[Requester: ${token.request.remoteHost}] - Successfully logged in subject [$apiSubjectInstance.id]$apiSubjectInstance.principal using api source"
    
    return account
  }

  private boolean validateRequestDate(long requestDate) {
    long date = System.currentTimeMillis()

    // Request must be within a minute
    if(requestDate < date) {
      return date - requestDate < 60000
    }
    
    requestDate - date < 60000
  }

  private boolean validateSignature(def request, String signature, ApiSubject apiSubjectInstance) {
    def method = request.method
    def date = request.getHeader('Date') ?: request.getHeader('X-AAF-Date')
    def remoteHost = request.remoteHost
    def path = request.forwardURI
    def contentType = request.contentType
    def body = request.inputStream.text

    def input = new StringBuffer()
    input << method.toLowerCase().trim() << "\n"
    input << remoteHost.toLowerCase().trim() << "\n"
    input << path.toLowerCase().trim() << "\n"
    input << date.toLowerCase().trim() << "\n"
    
    if(method == "POST" || method == "PUT") {
      input << contentType?.toLowerCase().trim() << "\n"
      input << body?.encodeAsSHA256() << "\n"
    }

    log.info "[Requester: ${request.remoteHost}] - Using following input to calculate signature:\n$input"

    String computedSignature = calculateHMAC(apiSubjectInstance.apiKey, input.toString())
    log.info ("[Requester: ${request.remoteHost}] - Calculated signature of $computedSignature - comparing to supplied signature $signature")
    computedSignature == signature
  }

  private String calculateHMAC(String secret, String input) {
    def signingKey = new SecretKeySpec(secret.getBytes(), 'HmacSHA256')
    def mac = Mac.getInstance('HmacSHA256')
    mac.init(signingKey)
    def rawHmac = mac.doFinal(input.getBytes())
    
    rawHmac.encodeBase64().toString()
  }

}
