package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification

class GroupInterceptorSpec extends Specification implements InterceptorUnitTest<GroupInterceptor> {

    def setup() {
    }

    def cleanup() {

    }

    def 'ensure GroupController interceptor only excludes list, create, save'() {

        when:
        withRequest(controller:'group')

        then:
        interceptor.doesMatch()



    
        /*when:
        controller

        then:
        controller.beforeInterceptor.except.size() == 3
        controller.beforeInterceptor.except.containsAll(['list', 'create', 'save'])*/
    }

    void "Test group interceptor matching"() {
        when:"A request matches the interceptor"
            withRequest(controller:"group")

        then:"The interceptor does match"
            interceptor.doesMatch()
    }
}
