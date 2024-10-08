package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class EmailTemplateInterceptorSpec extends Specification implements InterceptorUnitTest<EmailTemplateInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test emailTemplate interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"emailTemplate")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
