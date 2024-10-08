package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class WorkflowProcessInterceptorSpec extends Specification implements InterceptorUnitTest<WorkflowProcessInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test workflowProcess interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"workflowProcess")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
