package virtualhome

import grails.testing.web.interceptor.InterceptorUnitTest
import spock.lang.Specification
import grails.testing.gorm.DomainUnitTest
import aaf.base.admin.EmailTemplate

class EmailTemplateInterceptorSpec extends Specification implements InterceptorUnitTest<EmailTemplateInterceptor>, DomainUnitTest<EmailTemplate> {

    def 'Ensure that if no ID is given, the interceptor redirects us to emailTemplateNoID'() {
        when:
        interceptor.before()

        then:
        response.redirectedUrl == "/emailTemplate/emailTemplateNoID"
    }

    def 'Ensure that if no EmailTemplate is found for an ID, the interceptor redirects us to emailTemplateNoTemplate'() {
        when:
        params.id = 1
        interceptor.before()

        then:
        response.redirectedUrl == "/emailTemplate/emailTemplateNoTemplate"
    }
}
