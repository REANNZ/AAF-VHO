package aaf.vhr

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.plugins.orm.auditable.Auditable

@ToString(includeNames=true, includes="id, subject")
@EqualsAndHashCode
class ChallengeResponse implements Auditable {
  String challenge
  String hash
  String salt
  
  Date dateCreated
  Date lastUpdated

  static belongsTo = [subject:ManagedSubject]
  static constraints = {
    challenge nullable: false, blank: false, minSize:6   
    hash nullable: false, blank:false, size: 128..128
    salt nullable: false, blank:false, size: 29..29
  }

  String response
  static transients = ['response']
}
