package aaf.vhr

import org.springframework.context.i18n.LocaleContextHolder
import org.apache.commons.validator.EmailValidator

import org.apache.shiro.SecurityUtils

import groovy.time.TimeCategory

import aaf.base.admin.EmailTemplate

class ManagedSubjectService {

  boolean transactional = true
  def grailsApplication
  def messageSource

  def sharedTokenService
  def emailManagerService
  def passwordValidationService
  def cryptoService

  private final String TOKEN_COUNT = 'aaf.vhr.managedsubjectservice.registerfromcsv.invalidtokens'
  private final String TOKEN_CN = 'aaf.vhr.managedsubjectservice.registerfromcsv.invalidcn'
  private final String TOKEN_EMAIL = 'aaf.vhr.managedsubjectservice.registerfromcsv.invalidemail'
  private final String TOKEN_AFFILIATION ='aaf.vhr.managedsubjectservice.registerfromcsv.invalidaffiliation'
  private final String TOKEN_EXPIRY ='aaf.vhr.managedsubjectservice.registerfromcsv.expiry'
  private final String TOKEN_LOGIN = 'aaf.vhr.managedsubjectservice.registerfromcsv.invalidlogin'
  private final String TOKEN_PASSWORD = 'aaf.vhr.managedsubjectservice.registerfromcsv.invalidpassword'

  private final String TOKEN_EMAIL_SUBJECT ='aaf.vhr.managedsubjectservice.registered.email.subject'

  private final String INVITATION_INVALID ='aaf.vhr.managedsubjectservice.finalize.invitation.invalid'

  public static final String[] AFFILIATIONS = [ 'faculty', 'student', 'staff', 'alum', 'member', 
                                                'affiliate', 'employee', 'library-walk-in' ]

  public static final String DEFAULT_ASSURANCE = 'urn:mace:aaf.edu.au:iap:id:1'

  def finalize(ManagedSubjectInvitation invitation, String login, String plainPassword, String plainPasswordConfirmation, String mobileNumber) {
    def managedSubject = invitation.managedSubject

    if(invitation.utilized || managedSubject.login != null)
      return [false, messageSource.getMessage(INVITATION_INVALID, [] as Object[], INVITATION_INVALID, LocaleContextHolder.locale)]

    managedSubject.login = login
    managedSubject.plainPassword = plainPassword
    managedSubject.plainPasswordConfirmation = plainPasswordConfirmation
    managedSubject.mobileNumber = mobileNumber
    
    def (valid, errors) = passwordValidationService.validate(managedSubject)
    if(!valid) {
      log.warn "Unable to finalize $managedSubject as password is invalid"
      return [false, managedSubject]
    }

    cryptoService.generatePasswordHash(managedSubject)
    managedSubject.active = true
    
    if(!managedSubject.save()) {
      log.error "Failed trying to save $managedSubject when finalizing"
      managedSubject.errors.each {
        log.warn it
      }

      throw new RuntimeException("Failed trying to save $managedSubject when finalizing")  // Rollback transaction
    }

    invitation.utilized = true
    if(!invitation.save()) {
      log.error "Failed trying to save $invitation when finalizing $managedSubject"
      managedSubject.errors.each {
        log.warn it
      }

      throw new RuntimeException("Failed trying to save $invitation when finalizing $managedSubject")  // Rollback transaction
    }

    // Clean up any unused invitations
    //ManagedSubjectInvitation.findAllWhere(managedSubject:managedSubject, utilized:false)*.delete()

    log.info "Finalized the account for $managedSubject - they are now ready to use VHR"
    return [true, managedSubject]
  }

  def register(ManagedSubject managedSubject, boolean confirm = true) {
    if(!managedSubject.save()) {
      log.error "Failed trying to save $managedSubject"
      managedSubject.errors.each {
        log.warn it
      }

      throw new RuntimeException("Failed trying to save $managedSubject")  // Rollback transaction
    }

    if(confirm)
      sendConfirmation(managedSubject)

    managedSubject
  }

  def registerFromCSV(Group group, byte[] csv) {

    def emailValidator = EmailValidator.getInstance()

    def valid = true
    def errors = [] as List
    def subjects = [] as List

    def lc = 0

    ByteArrayInputStream is = new ByteArrayInputStream(csv)
    is.eachCsvLine { tokens ->
      lc++

      // Ensure required pii
      if(tokens.size() != 4 && tokens.size() != 6) {
        valid = false
        errors.add(messageSource.getMessage(TOKEN_COUNT, [lc] as Object[], TOKEN_COUNT, LocaleContextHolder.locale))
      } else {
        // Only if we have the correct number of tokens do we look at actual content
        // Ensure cn format
        if(tokens[0].size() < 1 || tokens[0].count(' ') > 1) {
          valid = false
          errors.add(messageSource.getMessage(TOKEN_CN, [lc, tokens[0]] as Object[], TOKEN_CN, LocaleContextHolder.locale))
        }

        // Ensure email format
        if(tokens[1].size() < 1 || !emailValidator.isValid(tokens[1])) {
          valid = false
          errors.add(messageSource.getMessage(TOKEN_EMAIL, [lc, tokens[1]] as Object[], TOKEN_EMAIL, LocaleContextHolder.locale))
        }

        // Ensure affiliation
        if(tokens[2].size() < 1 || !ManagedSubjectService.AFFILIATIONS.contains(tokens[2])) {
          valid = false
          errors.add(messageSource.getMessage(TOKEN_AFFILIATION, [lc, tokens[2]] as Object[], TOKEN_AFFILIATION, LocaleContextHolder.locale))
        }

        // Ensure expiry
        if(tokens[3].size() < 1 || !tokens[3].isNumber()){
          valid = false
          errors.add(messageSource.getMessage(TOKEN_EXPIRY, [lc, tokens[2]] as Object[], TOKEN_EXPIRY, LocaleContextHolder.locale))
        }

        if(tokens.size() == 6) {
          if(SecurityUtils.subject.isPermitted("app:administrator")) {
            // Ensure login
            if(tokens[4].size() < 1){
              valid = false
              errors.add(messageSource.getMessage(TOKEN_LOGIN, [lc, tokens[4]] as Object[], TOKEN_LOGIN, LocaleContextHolder.locale))
            }

            // Ensure password
            if(tokens[5].size() < 8) {
              valid = false
              errors.add(messageSource.getMessage(TOKEN_PASSWORD, [lc, tokens[5]] as Object[], TOKEN_PASSWORD, LocaleContextHolder.locale))
            }
          } else {
            // for non admins report token size error so login/password functionality isn't leaked
            valid = false
            errors.add(messageSource.getMessage(TOKEN_COUNT, [lc, tokens[0]] as Object[], TOKEN_COUNT, LocaleContextHolder.locale))
          }
        }
      }
    }

    is.close()  // a no-op but incase this changes in the future

    if(!valid) {
      log.error "Unable to process CSV uploaded by $subject"
      errors.each {
        log.info it
      }
      return [false, errors, null, lc]
    }

    // Valid CSV data, create ManagedSubjects
    lc = 0
    is = new ByteArrayInputStream(csv)
    is.eachCsvLine { tokens ->
      lc++

      def managedSubject = new ManagedSubject(cn:tokens[0], email:tokens[1], eduPersonAffiliation:tokens[2], active:false, displayName:tokens[0], eduPersonAssurance: DEFAULT_ASSURANCE, organization:group.organization, group:group)
      sharedTokenService.generate(managedSubject)

      if(tokens[3].toInteger() > 0) {
        use(TimeCategory) {
          Date now = new Date()
          managedSubject.accountExpires = now + tokens[3].toInteger().months
        }
      }

      if(!managedSubject.validate()) {
        valid = false
      }

      if(tokens.size() == 6) {
        managedSubject.active = true
        managedSubject.login = tokens[4]
        managedSubject.plainPassword = tokens[5]
        managedSubject.plainPasswordConfirmation = tokens[5]

        if(!managedSubject.validate()) {
          valid = false
        }

        def (validPassword, passwordErrors) = passwordValidationService.validate(managedSubject)
        if(!validPassword) {
          log.error "Error in password supplied for $managedSubject"
          valid = false
        } else {
          cryptoService.generatePasswordHash(managedSubject)
        }
      }

      subjects.add(managedSubject)
    }

    if(valid) {
      // There is of course a small chance that someone else could create a ManagedSubject
      // with the same values in the interim but it is unlikely.
      subjects.each { managedSubject ->
        managedSubject = register(managedSubject, false)
        log.info "Created $managedSubject from CSV file submitted by $subject"
      }

      log.info "Created all subjects from CSV file submitted by $subject"
      subjects.each { ms ->
        if(!ms.login) {
          log.info "Email account information and further instructions to $ms"
          sendConfirmation(ms)
        } else {
          log.info "As account $ms has been provided login and password no email details where sent."
        }
      }

      return [true, errors, subjects, lc]
    }
    
    [false, errors, subjects, lc]
  }

  public void sendConfirmation(ManagedSubject managedSubject) {
    def emailSubject = messageSource.getMessage(TOKEN_EMAIL_SUBJECT, [] as Object[], TOKEN_EMAIL_SUBJECT, LocaleContextHolder.locale)
    def emailTemplate = EmailTemplate.findWhere(name:"registered_managed_subject")

    if(!emailTemplate) {
      throw new RuntimeException("Email template for creating new ManagedSubjects 'registered_managed_subject' does not exist")  // Rollback transaction
    }

    def invitation = new ManagedSubjectInvitation(managedSubject:managedSubject)
    if(!invitation.save()) {
      log.error "Failed to create invitation code for $managedSubject aborting"
      invitation.errors.each {
        log.warn it
      }
      throw new RuntimeException("Failed to create invitation code for $managedSubject aborting")  // Rollback transaction
    }
    emailManagerService.send(managedSubject.email, emailSubject, emailTemplate, [managedSubject:managedSubject, invitation:invitation]) 
  }

}
