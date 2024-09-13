package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

@TestFor(aaf.base.identity.Role)
class RoleSpec extends Specification {
  
  def 'Ensure Role wont validate with null name'() {
    setup:
    mockDomain(Role)
    
    when:
    def r = new Role().validate()
    
    then:
    !r
  }
  
  def 'Ensure Role wont validate with blank name'() {
    setup:
    mockDomain(Role)
    
    when:
    def r = new Role(name:'').validate()
    
    then:
    !r
  }
  
  def 'Ensure Role wont validate with non-unique name'() {
    setup:
    mockDomain(Role)
    
    when:
    new Role(name:'testrole').save(flush: true)
    def r = new Role(name:'testrole').validate()
    
    then:
    !r
  }
  
  def 'Ensure Role will validate'() {
    setup:
    mockDomain(Role)
    
    when:
    def r = new Role(name:'testrole').validate()
    
    then:
    r
  }

}
