package org.grails.plugins.sanitizer

import org.springframework.core.io.FileSystemResource

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import spock.lang.*

@Integration
@Rollback
class MarkupSanitizerServiceIntegrationTests extends Specification {

	def markupSanitizerService

	void setup() {
		markupSanitizerService.markupSanitizer = new AntiSamyMarkupSanitizer(new FileSystemResource("src/main/scripts/antisamyConfigs/antisamy-myspace-1.4.4.xml"))
	}

	def 'test service is alive'() {
		assertNotNull(markupSanitizerService)
	}

	/**
	 * Simple helper method for tests
	 * @param expectation
	 * @param testString
	 */
	void assertSanitized(String expectation, String testString){
		assertEquals(expectation, markupSanitizerService.sanitize(testString).cleanString)
	}

	/**
	 * Simple helper method for validate tests
	 * @param expectation
	 * @param testString
	 */
	void assertValidTrue(String testString){
		def result = markupSanitizerService.validateMarkup(testString)

		if(result.isInvalidMarkup()){
			println(result.errorMessages)
		}

		assertFalse(result.isInvalidMarkup())
	}

	def 'test sanitize HTML script tag'(){
		assertSanitized("<div>sanitize</div>", "<script></script><div>sanitize</div>")
	}

	void 'test sanitize HTML script tag with errors'(){
		MarkupSanitizerResult result = markupSanitizerService.sanitize("<script><script><div>sanitize</div>")
		assertTrue(result.isInvalidMarkup())
	}

	void 'test validate HTML script tag with errors'(){
		MarkupValidatorResult result = markupSanitizerService.validateMarkup("<script><script><div>sanitize</div>")
		assertTrue(result.isInvalidMarkup())
	}
}
