package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class LoginApiInterceptorSpec extends Specification implements InterceptorUnitTest<LoginApiInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test loginApi interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"loginApi")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
