package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
public class FederatedToken implements org.apache.shiro.authc.AuthenticationToken {
  def principal, credential, sharedToken, attributes, remoteHost, userAgent

  public Object getCredentials() {
    return this.credential
  }
  
  public Object getPrincipal() {
    return this.principal
  }  
}
