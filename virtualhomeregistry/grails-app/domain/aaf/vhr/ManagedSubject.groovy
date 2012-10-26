package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

import aaf.base.identity.Subject

@ToString(includeNames=true, includeFields=true, excludes="pii, hash, plainPassword, plainPasswordConfirmation")
@EqualsAndHashCode
class ManagedSubject {
  String login
  String hash

  String cn     // per oid:2.5.4.3
  String email  // per oid:0.9.2342.19200300.100.1.3
  
  List pii
  List challengeResponse
  List emailReset

  static hasMany = [pii: AttributeValue,  // Personally Identifiable Information (PII)
                    challengeResponse: ChallengeResponse,
                    emailReset: EmailReset]  

  static constraints = {
    login nullable:true, blank: false, unique: true, size: 5..100
    hash blank:false, minSize:60, maxSize:60
    email blank:false, unique:true, email:true
    cn validator: {val, obj ->
      return (val != null && val != '' && (val.count(' ') == 0 || val.count(' ') == 1))
    }
  }

  String plainPassword
  String plainPasswordConfirmation
  static transients = ['plainPassword', 'plainPasswordConfirmation']
}
