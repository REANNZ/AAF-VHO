package aaf.vhr

import grails.transaction.Transactional
import org.springframework.context.i18n.LocaleContextHolder

import aaf.base.admin.EmailTemplate

@Transactional
class LostPasswordService {

  def emailManagerService
  def messageSource
  def grailsLinkGenerator

  private final String EMAIL_SUBJECT ='aaf.vhr.lostpasswordservice.email.subject'

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
      throw new RuntimeException("Email template for advising about deactivated accounts 'deactivated_managed_subject' does not exist")  // Rollback transaction
    }

    def emailSubject = messageSource.getMessage(EMAIL_SUBJECT, [] as Object[], EMAIL_SUBJECT, LocaleContextHolder.locale)
    def url = grailsLinkGenerator.link(controller: 'lostpassword', action: 'reset', absolute: true, params: [emailClicked:true])

    emailManagerService.send(managedSubject.email, emailSubject, emailTemplate, [managedSubject:managedSubject, emailURL:url])
  }

  public void sendResetEmailWithoutLink(ManagedSubject managedSubject) {
    log.info "Sending password reset email for user ${managedSubject.cn} with no phone number."

    def emailTemplate = EmailTemplate.findWhere(name:'email_password_reset_without_link')
    if(!emailTemplate) {
      throw new RuntimeException("Email template for advising about deactivated accounts 'deactivated_managed_subject' does not exist")  // Rollback transaction
    }

    def emailSubject = messageSource.getMessage(EMAIL_SUBJECT, [] as Object[], EMAIL_SUBJECT, LocaleContextHolder.locale)
    emailManagerService.send(managedSubject.email, emailSubject, emailTemplate, [managedSubject:managedSubject])
  }
}
