package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class ConsoleBootstrapInterceptorSpec extends Specification implements InterceptorUnitTest<ConsoleBootstrapInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test consoleBootstrap interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"consoleBootstrap")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
