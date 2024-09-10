package aaf.base.identity

import groovy.transform.EqualsAndHashCode
import groovy.transform.ToString

@ToString(includeNames=true, includeFields=true)
@EqualsAndHashCode
public class ApiToken implements org.apache.shiro.authc.AuthenticationToken {
  
  // Parameters
  def principal, signature, timestamp
  
  // HttpServletRequest
  def request

  public Object getCredentials() {
    return this.signature
  }
  
  public Object getPrincipal() {
    return this.principal
  }  
}
