package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class OrganizationInterceptorSpec extends Specification implements InterceptorUnitTest<OrganizationInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test organization interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"organization")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
