package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class MigrateInterceptorSpec extends Specification implements InterceptorUnitTest<MigrateInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test migrate interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"migrate")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
