package aaf.base.identity

import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

@TestFor(aaf.base.identity.SessionRecord)
class SessionRecordSpec extends Specification {
  
  def 'Ensure SessionRecord wont validate with null credential'() {
    setup:
    mockDomain(SessionRecord)
    
    when:
    def sr = new SessionRecord(remoteHost:'test', userAgent:'test').validate()
    
    then:
    !sr
  }
  
  def 'Ensure SessionRecord wont validate with blank credential'() {
    setup:
    mockDomain(SessionRecord)
    
    when:
    def sr = new SessionRecord(credential:'', remoteHost:'test', userAgent:'test').validate()
    
    then:
    !sr
  }
  
  def 'Ensure SessionRecord wont validate null remoteHost credential'() {
    setup:
    mockDomain(SessionRecord)
    
    when:
    def sr = new SessionRecord(credential:'test', userAgent:'test').validate()
    
    then:
    !sr
  }
  
  def 'Ensure SessionRecord wont validate with blank remoteHost'() {
    setup:
    mockDomain(SessionRecord)
    
    when:
    def sr = new SessionRecord(credential:'test', remoteHost:'', userAgent:'test').validate()
    
    then:
    !sr
  }
  
  def 'Ensure SessionRecord wont validate null userAgent credential'() {
    setup:
    mockDomain(SessionRecord)
    
    when:
    def sr = new SessionRecord(credential:'test', remoteHost:'test').validate()
    
    then:
    !sr
  }
  
  def 'Ensure SessionRecord wont validate with blank userAgent'() {
    setup:
    mockDomain(SessionRecord)
    
    when:
    def sr = new SessionRecord(credential:'test', remoteHost:'test', userAgent:'').validate()
    
    then:
    !sr
  }

}
