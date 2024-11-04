package aaf.vhr

import groovy.time.TimeCategory
import aaf.base.identity.SessionRecord

class FinalizationController {

  static allowedMethods = [complete: 'POST']

  static final MANAGED_SUBJECT_ID = 'aaf.vhr.FinalizationController.MANAGED_SUBJECT_ID'

  def managedSubjectService
  
  def index(String inviteCode) {
    def invitationInstance = ManagedSubjectInvitation.findWhere(inviteCode:inviteCode)

    if(!invitationInstance) {
      log.error "no such invitation exists"
      redirect action: 'error'
      return
    }

    if(invitationInstance.utilized) {
      redirect action: 'used'
      return
    }

    session.setAttribute(MANAGED_SUBJECT_ID, invitationInstance?.managedSubject?.id)

    [managedSubjectInstance:invitationInstance.managedSubject, invitationInstance:invitationInstance]
  }

  def loginAvailable(String login) {
    if(!login.matches('\\A[_a-zA-Z][-._a-zA-Z0-9]*\\z')) {
      render "false"
      return
    }

    def id = session.getAttribute(MANAGED_SUBJECT_ID)
    if (!id) {
      render status: '403'
      return
    }

    def managedSubjectInstance = ManagedSubject.findWhere(login:login)
    if (!managedSubjectInstance) {
      render "true"
      return
    }

    if (managedSubjectInstance.id != id) {
      render "false"
      return
    }

    render "true"
  }

  def complete(String inviteCode, String login, String plainPassword, String plainPasswordConfirmation, String mobileNumber) {
    def invitationInstance = ManagedSubjectInvitation.findWhere(inviteCode:inviteCode)

    if(!invitationInstance) {
      log.error "no such invitation exists"
      redirect action: 'error'
      return
    }

    session.removeAttribute(MANAGED_SUBJECT_ID)

    def (outcome, managedSubjectInstance) = managedSubjectService.finalize(invitationInstance, login, plainPassword, plainPasswordConfirmation, mobileNumber ?:null)
    if(!outcome) {
      render (view: 'index', model:[managedSubjectInstance:managedSubjectInstance, invitationInstance:invitationInstance])
      return
    }
    [managedSubjectInstance: managedSubjectInstance]
  }

  def used() {
  }

  def error() {
  }
}
