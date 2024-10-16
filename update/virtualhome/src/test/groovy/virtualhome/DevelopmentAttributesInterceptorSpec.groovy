package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class DevelopmentAttributesInterceptorSpec extends Specification implements InterceptorUnitTest<DevelopmentAttributesInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test developmentAttributes interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"developmentAttributes")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
