package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class WorkflowScriptInterceptorSpec extends Specification implements InterceptorUnitTest<WorkflowScriptInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test workflowScript interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"workflowScript")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
