package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class LogoutInterceptorSpec extends Specification implements InterceptorUnitTest<LogoutInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test logout interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"logout")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
