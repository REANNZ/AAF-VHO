package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class InvitedAdministratorAuthenticatedInterceptorSpec extends Specification implements InterceptorUnitTest<InvitedAdministratorAuthenticatedInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    void "Test invitedAdministratorAuthenticated interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"invitedAdministratorAuthenticated")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
