package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ConsoleInterceptorSpec extends Specification implements InterceptorUnitTest<ConsoleInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test console interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"console")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
