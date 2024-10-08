package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class RoleInterceptorSpec extends Specification implements InterceptorUnitTest<RoleInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test role interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"role")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
