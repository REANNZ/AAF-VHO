package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import aaf.base.identity.Subject

@ToString(includeNames=true, includes="id, login, cn, email")
@EqualsAndHashCode
class ManagedSubject {
  static auditable = true

  static final affiliations = [ 'affiliate',
                                'alum',
                                'employee',
                                'faculty',
                                'library-walk-in',
                                'member',
                                'staff',
                                'student'] as List

  String login
  String hash

  String apiKey               // Use for local account management context

  // Password reset. Both codes required to be input, second provided via SMS or administrator
  String resetCode
  String resetCodeExternal           

  // AAF Core
  String cn                   // oid:2.5.4.3
  String email                // oid:0.9.2342.19200300.100.1.3
  String sharedToken          // oid:1.3.6.1.4.1.27856.1.2.5
  String displayName          // oid:2.16.840.1.113730.3.1.241
  String eduPersonAssurance   // oid:1.3.6.1.4.1.5923.1.1.1.11
  String eduPersonAffiliation // oid:1.3.6.1.4.1.5923.1.1.1.1 - stored seperated by ; for IdP resolver simplification
  String eduPersonEntitlement // oid:1.3.6.1.4.1.5923.1.1.1.7 - stored seperated by ; for IdP resolver simplification
  
  // AAF Optional
  String givenName            // oid:2.5.4.42
  String surname              // oid:2.5.4.4
  String mobileNumber         // oid:0.9.2342.19200300.100.1.41
  String telephoneNumber      // oid:2.5.4.20
  String postalAddress        // oid:2.5.4.16
  String organizationalUnit   // oid:2.5.4.11

  boolean active = false
  boolean locked = false

  int failedLogins = 0
  int failedResets = 0

  List challengeResponse
  List emailReset

  static hasMany = [challengeResponse: ChallengeResponse,
                    emailReset: EmailReset,
                    invitations: ManagedSubjectInvitation,
                    activeChanges: ManagedSubjectStateChange,
                    lockedChanges: ManagedSubjectStateChange]  

  static belongsTo = [organization:Organization,
                      group:Group]

  static constraints = {
    login nullable:true, blank: false, unique: true, size: 3..100,  validator: { val -> if (val?.contains(' ')) return 'value.contains.space' }
    hash nullable:true, blank:false, minSize:60, maxSize:60
    
    resetCode nullable:true
    resetCodeExternal nullable:true

    email blank:false, unique:true, email:true
    cn validator: {val, obj ->
      return (val != null && val != '' && (val.count(' ') == 0 || val.count(' ') == 1))
    }
    sharedToken nullable:false, blank: false, unique: true
    eduPersonEntitlement nullable:true, blank:false

    eduPersonAssurance inList: ['urn:mace:aaf.edu.au:iap:id:1',
                                'urn:mace:aaf.edu.au:iap:id:2',
                                'urn:mace:aaf.edu.au:iap:id:3',
                                'urn:mace:aaf.edu.au:iap:id:4']

    eduPersonAffiliation nullable:false, blank:false, maxSize: 255

    mobileNumber nullable: true, blank: false, validator: validMobileNumber
    givenName nullable: true, blank: false          
    surname nullable: true, blank: false            
    telephoneNumber nullable: true, blank: false   
    postalAddress nullable: true, blank: false      
    organizationalUnit nullable: true, blank: false 

    organization nullable: false
    group nullable: false
  }

  static mapping = {
    eduPersonEntitlement type: "text"
  }

  String plainPassword
  String plainPasswordConfirmation
  static transients = ['plainPassword', 'plainPasswordConfirmation']

  public ManagedSubject() {
    if(!this.apiKey)
      this.apiKey = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(16)
  }

  public canChangePassword() {
    !locked && organization?.functioning() && group?.functioning()
  }

  public boolean functioning() {
    active && !locked && organization?.functioning() && group?.functioning()
  }

  public void setResetCode(String resetCode) {
    this.resetCode = cleanCode(resetCode)
  }

  public void setResetCodeExternal(String resetCodeExternal) {
    this.resetCodeExternal = cleanCode(resetCodeExternal)
  }

  private String cleanCode(String code) {
    // Ensure no confusion on SMS/Email codes between
    // characters that look the same - extend as

    if (code.contains('I')) {
      code = code.replace('I', 'i')
    }
    if(code.contains('l')) {
      code = code.replace('l', 'L')
    }
    if(code.contains('O')) {
      code = code.replace('O', 'o')
    }
    if(code.contains('0')) {
      code = code.replace('0', '9')
    }

    code
  }

  static validMobileNumber = { value, obj ->
    if(value == "" || value == null) {
      obj.mobileNumber = null
      return true
    }

    def checkedNumber = value

    // Translate Australian numbers to international format
    if(checkedNumber.startsWith('04')) {
      checkedNumber = checkedNumber[1..-1]
      checkedNumber = "+61$checkedNumber"
    }

    if(!checkedNumber.startsWith('+')) {
      return false
    } else {
      obj.mobileNumber = checkedNumber
      return true
    }
  }
}
