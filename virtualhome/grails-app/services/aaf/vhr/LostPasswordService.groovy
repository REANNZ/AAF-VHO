package aaf.vhr

import grails.transaction.Transactional
import org.springframework.context.i18n.LocaleContextHolder

import aaf.base.admin.EmailTemplate
import aaf.base.identity.Role

@Transactional
class LostPasswordService {

  def emailManagerService
  def messageSource
  def grailsLinkGenerator

  private final String EMAIL_SUBJECT ='aaf.vhr.lostpasswordservice.email.subject'

  private String generateEmailLink(ManagedSubject managedSubject) {

    // Generate a secret embedded in the URL.
    // We store it in the resetCode parameter since its original purpose is not required any more.
    // This saves us from having to create a new database schema.
    def code = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(128)
    managedSubject.resetCode = code
    return grailsLinkGenerator.link(controller: 'lostpassword', action: 'obtainsubject', absolute: true, params: [code: code])
  }

  public void sendResetEmail(ManagedSubject managedSubject) {
    if (managedSubject.mobileNumber) {
        sendResetEmailWithLink(managedSubject)
    } else {
        sendResetEmailWithoutLink(managedSubject)
    }
  }

  public void sendResetEmailWithLink(ManagedSubject managedSubject) {
    log.info "Sending password reset email for user ${managedSubject.cn} with phone number ${managedSubject.mobileNumber} ."

    def emailTemplate = EmailTemplate.findWhere(name:'email_password_reset_with_link')
    if(!emailTemplate) {
      throw new RuntimeException("Email template for advising about deactivated accounts 'email_password_reset_with_link' does not exist")  // Rollback transaction
    }

    def emailSubject = messageSource.getMessage(EMAIL_SUBJECT, [] as Object[], EMAIL_SUBJECT, LocaleContextHolder.locale)
    def url = generateEmailLink(managedSubject)

    emailManagerService.send(managedSubject.email, emailSubject, emailTemplate, [managedSubject:managedSubject, emailURL:url])
  }

  public void sendResetEmailWithoutLink(ManagedSubject managedSubject) {
    log.info "Sending password reset email for user ${managedSubject.cn} with no phone number."

    def emailTemplate = EmailTemplate.findWhere(name:'email_password_reset_without_link')
    if(!emailTemplate) {
      throw new RuntimeException("Email template for advising about deactivated accounts 'email_password_reset_without_link' does not exist")  // Rollback transaction
    }

    def groupAdminRole = Role.findWhere(name:"group:${managedSubject.group.id}:administrators")
    def organizationAdminRole = Role.findWhere(name:"organization:${managedSubject.organization.id}:administrators")

    def emailSubject = messageSource.getMessage(EMAIL_SUBJECT, [] as Object[], EMAIL_SUBJECT, LocaleContextHolder.locale)
    emailManagerService.send(managedSubject.email, emailSubject, emailTemplate, [managedSubject:managedSubject, groupAdminRole:groupAdminRole, organizationAdminRole:organizationAdminRole])
  }
}
