package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class SecureResponseInterceptorSpec extends Specification implements InterceptorUnitTest<SecureResponseInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test secureResponse interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"secureResponse")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
