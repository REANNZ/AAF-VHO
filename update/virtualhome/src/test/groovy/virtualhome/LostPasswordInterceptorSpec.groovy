package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class LostPasswordInterceptorSpec extends Specification implements InterceptorUnitTest<LostPasswordInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test lostPassword interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"lostPassword")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
