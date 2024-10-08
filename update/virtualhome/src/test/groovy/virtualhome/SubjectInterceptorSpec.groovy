package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class SubjectInterceptorSpec extends Specification implements InterceptorUnitTest<SubjectInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test subject interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"subject")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
