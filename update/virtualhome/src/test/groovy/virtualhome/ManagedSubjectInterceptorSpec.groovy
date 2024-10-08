package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ManagedSubjectInterceptorSpec extends Specification implements InterceptorUnitTest<ManagedSubjectInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test managedSubject interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"managedSubject")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
