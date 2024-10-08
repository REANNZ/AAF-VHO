package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ApiSubjectInterceptorSpec extends Specification implements InterceptorUnitTest<ApiSubjectInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test apiSubject interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"apiSubject")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
