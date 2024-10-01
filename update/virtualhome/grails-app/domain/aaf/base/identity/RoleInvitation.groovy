package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString
import grails.plugins.orm.auditable.Auditable

import java.security.SecureRandom;
import java.util.Random;

@ToString(includeNames=true, includes="id, utilized, inviteCode")
@EqualsAndHashCode
class RoleInvitation implements Auditable {
  private static final SecureRandom SECUERANDOM = new SecureRandom();

  String redirectTo
  String inviteCode
  boolean utilized

  Date dateCreated
  Date lastUpdated

  static belongsTo = [role:Role]

  public RoleInvitation() {
    this.inviteCode = org.apache.commons.lang.RandomStringUtils.random(24, 0, 0, true, true, null, SECUERANDOM);
    this.utilized = false
  }
  
  static constraints = {
    role(nullable:false)
    redirectTo(nullable:false)
    inviteCode(nullable:false, unique:true)
  }
}
