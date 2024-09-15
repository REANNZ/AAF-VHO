package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import grails.testing.gorm.DomainUnitTest
import spock.lang.Stepwise

@Stepwise
class RoleSpec extends Specification implements DomainUnitTest<Role> {
  
  def 'Ensure Role wont validate with null name'() {
    when:
    def r = new Role().validate()
    
    then:
    !r
  }
  
  def 'Ensure Role wont validate with blank name'() {
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
    when:
    def r = new Role(name:'testrole').validate()
    
    then:
    r
  }

}
