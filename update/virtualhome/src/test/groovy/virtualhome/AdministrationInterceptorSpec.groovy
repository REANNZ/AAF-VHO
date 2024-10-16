package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class AdministrationInterceptorSpec extends Specification implements InterceptorUnitTest<AdministrationInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test administration interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"administration")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
