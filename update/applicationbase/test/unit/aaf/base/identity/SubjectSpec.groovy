package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

@TestFor(aaf.base.identity.Subject)
class SubjectSpec extends spock.lang.Specification {
  
  def 'Ensure subject wont validate with null principal'() {    
    when:
    def s = new Subject().validate()
    
    then:
    !s
  }
  
  def 'Ensure subject wont validate with blank principal'() {
    when:
    def s = new Subject(principal:'').validate()
    
    then:
    !s
  }
  
  def 'Ensure subject wont validate with non-unique principal'() {    
    when:
    def s1 = new Subject(principal:'http://test.edu.au!http://sp.test.edu.au!1234').save(flush: true)
    def s2 = new Subject(principal:'http://test.edu.au!http://sp.test.edu.au!1234')
    
    then:
    !s1.hasErrors()
    !s2.validate()
  }
  
  def 'Ensure subject will validate'() {    
    when:
    def s = new Subject(principal:'http://test.edu.au!http://sp.test.edu.au!1234', cn:'Test User', email:'testuser@test.edu.au', sharedToken:'34wrder').validate()
    
    then:
    s
  }

}
