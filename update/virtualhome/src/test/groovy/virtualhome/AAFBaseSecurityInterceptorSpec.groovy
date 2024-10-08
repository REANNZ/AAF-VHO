package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AAFBaseSecurityInterceptorSpec extends Specification implements InterceptorUnitTest<AAFBaseSecurityInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test AAFBaseSecurity interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"AAFBaseSecurity")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
