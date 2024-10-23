package aaf.vhr

import grails.transaction.Transactional

import aaf.base.admin.EmailTemplate

@Transactional
class LostPasswordService {

  def emailManagerService

  public void sendResetEmail(ManagedSubject managedSubject) {
    if (managedSubject.mobileNumber) {
        sendResetEmailWithLink(managedSubject)
    } else {
        sendResetEmailWithoutLink(managedSubject)
    }
  }

  public void sendResetEmailWithLink(ManagedSubject managedSubject) {
    log.info "Sending password reset email for user ${managedSubject.cn} with phone number ${managedSubject.mobileNumber} ."
  }

  public void sendResetEmailWithoutLink(ManagedSubject managedSubject) {
    log.info "Sending password reset email for user ${managedSubject.cn} with no phone number."
  }
}
