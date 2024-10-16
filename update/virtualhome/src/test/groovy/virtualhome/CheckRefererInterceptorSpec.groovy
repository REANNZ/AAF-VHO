package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class CheckRefererInterceptorSpec extends Specification implements InterceptorUnitTest<CheckRefererInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test checkReferer interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"checkReferer")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
