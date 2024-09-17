import grails.test.mixin.*
import spock.lang.*
import grails.test.spock.*

import org.apache.shiro.authc.*
import aaf.base.identity.*

import grails.util.GrailsWebUtil
import org.springframework.mock.web.MockHttpServletRequest
import java.text.SimpleDateFormat


import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*

@Integration
@Rollback
class ApiRealmSpec extends Specification {

  /*
   TODO: Due to time pressure to ship VHR into test i'll 
   have come back and do this once that occurs. 
   
   It sucks I agree.
  */

}
