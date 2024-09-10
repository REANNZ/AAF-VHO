package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
class SessionRecord {
  static auditable = true
  
  String credential
  String remoteHost
  String userAgent
  
  Date dateCreated

  static belongsTo = [subject: Subject]

  static constraints = {
    credential(nullable: false, blank: false)
    remoteHost(nullable: false, blank: false)
    userAgent(nullable: false, blank: false)
  }

  static mapping = {
    table 'base_sessionrecord'
  }
}
