package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AdministrationAuthenticatedInterceptorSpec extends Specification implements InterceptorUnitTest<AdministrationAuthenticatedInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test administrationAuthenticated interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"administrationAuthenticated")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
