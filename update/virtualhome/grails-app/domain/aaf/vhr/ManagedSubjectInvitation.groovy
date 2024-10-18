package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.plugins.orm.auditable.Auditable

@ToString(includeNames=true, includes="id, utilized, inviteCode")
@EqualsAndHashCode
class ManagedSubjectInvitation implements Auditable {
  String inviteCode
  boolean utilized

  Date dateCreated
  Date lastUpdated

  static belongsTo = [managedSubject:ManagedSubject]

  public ManagedSubjectInvitation() {
    this.inviteCode = aaf.vhr.crypto.CryptoUtil.randomAlphanumeric(24)
    this.utilized = false
  }
  
  static constraints = {
    managedSubject(nullable:false)
    inviteCode(nullable:false, unique:true)
  }
}
