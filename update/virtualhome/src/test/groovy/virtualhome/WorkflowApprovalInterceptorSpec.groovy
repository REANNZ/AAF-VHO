package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class WorkflowApprovalInterceptorSpec extends Specification implements InterceptorUnitTest<WorkflowApprovalInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test workflowApproval interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"workflowApproval")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
