package aaf.vhr.api.v1

import org.springframework.dao.DataIntegrityViolationException
import org.apache.shiro.SecurityUtils

import aaf.base.identity.Role
import aaf.base.identity.Permission

import aaf.vhr.ManagedSubject
import aaf.vhr.AuthnTuple

import groovy.json.*

import java.text.SimpleDateFormat
import java.util.TimeZone

class LoginApiController extends aaf.base.api.ApiBaseController {

  static allowedMethods = [confirmsession: 'GET', basicauth:'POST']

  def beforeInterceptor = [action: this.&validateRequest]

  def loginService
  def cryptoService

  // Ancillary object for Date formatting
  def sdf = getUTCSimpleDateFormatter()
  def SimpleDateFormat getUTCSimpleDateFormatter() {
      // SimpleDateFormatter for ISO 8601 timestamps with milliseconds
      def _sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
      // Set the timezone to UTC - putting initialization into ()
      _sdf.setTimeZone(TimeZone.getTimeZone("UTC"))
      _sdf
  }

  def confirmsession(String sessionID) {
    def remoteUserTuple = loginService.sessionRemoteUser(params.sessionID)

    if(remoteUserTuple) {
      log.info "API session verification for ${params.sessionID} provided response with remote_user of $remoteUserTuple.username"
      render(contentType: 'application/json') { ['remote_user':remoteUserTuple.username, 'authnInstant':sdf.format(remoteUserTuple.authnInstant), 'mfa':remoteUserTuple.mfa] }
    } else {
      log.error "API session verification for ${params.sessionID} failed, providing error response"
      response.status = 410
      render(contentType: 'text/json') { ['error':'session could not be validated', 'internalerror':'login service session cache indicates no associated session for the supplied id'] }
    }
  }

  def basicauth(String login, String password) {
    log.info "API basic auth verification for ${login} is being attempted"
    def managedSubjectInstance = ManagedSubject.findByLogin(login)
    if(managedSubjectInstance != null) {
      if(managedSubjectInstance.canLogin()) {
        // Unfortunately there is no way to validate accounts with 2-Step active
        // so we ignore the extra security (derrrrp - hate the basic auth)
        if(cryptoService.verifyPasswordHash(password, managedSubjectInstance)) {
          log.info "API basicauth session establishment for $login succeeded. Provided response with remote_user of $login"
          render(contentType: 'application/json') { ['remote_user':login] }
        } else {
          log.error "API basic auth verification for ${login} failed, could not validate supplied credential, providing error response"
          response.status = 403
          render(contentType: 'text/json') { ['error':'basic auth session could not be established', 'internalerror':'supplied credential could not be validated'] }
        }
      } else {
        log.error "API basic auth session verification for ${login} failed, account is unable to authenticate at this time, providing error response"
        response.status = 403
        render(contentType: 'text/json') { ['error':'basic auth session could not be established', 'internalerror':'account is unable to authenticate at this time - check VH interface for details'] }
      }
    } else {
      log.error "API basic auth session verification for ${login} failed, could not locate matching ManagedSubject, providing error response"
      response.status = 410
      render(contentType: 'text/json') { ['error':'basic auth session could not be established', 'internalerror':'no associated account for the supplied login'] }
    }
  }

}
