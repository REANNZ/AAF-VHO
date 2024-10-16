package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class WorkflowAuthenticatedInterceptorSpec extends Specification implements InterceptorUnitTest<WorkflowAuthenticatedInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test workflowAuthenticated interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"workflowAuthenticated")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
