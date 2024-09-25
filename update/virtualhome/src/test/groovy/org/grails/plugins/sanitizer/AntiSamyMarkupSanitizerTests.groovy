package org.grails.plugins.sanitizer

import grails.test.*

import org.springframework.core.io.FileSystemResource

import spock.lang.*

class AntiSamyMarkupSanitizerTests extends Specification {

	@Shared def sanitizer

	def setup() {
		sanitizer = new AntiSamyMarkupSanitizer(
			new FileSystemResource("scripts/antisamyConfigs/antisamy-myspace-1.4.4.xml"))
	}

	/**
	 * Simple helper method for sanitize tests
	 * @param expectation
	 * @param testString
	 */
	def assertSanitized(String expectation, String testString){
		assertEquals(expectation, sanitizer.sanitize(testString).cleanString)
	}

	/**
	 * Simple helper method for validate tests
	 * @param expectation
	 * @param testString
	 */
	def assertValidTrue(String testString){
		assertFalse(sanitizer.validateMarkup(testString).isInvalidMarkup())
	}

	def 'testSanitizeSanity'(){
		expect:
		assertSanitized("sanitize", "sanitize")
	}

	def 'testValidateSanity'(){
		expect:
		assertValidTrue("sanitize")
	}

	def 'testSanitizeHtml'(){
		expect:
		assertSanitized("<div>sanitize</div>", "<div>sanitize</div>")
	}

	def 'testValidateHtml'(){
		expect:
		assertValidTrue("<div>sanitize</div>")
	}

	def 'testSanitizeHtmlScriptTag'(){
		expect:
		assertSanitized("<div>sanitize</div>", "<script></script><div>sanitize</div>")
	}

	def 'testSanitizeHtmlScriptTagWithErrors'(){
		setup:
		def result = sanitizer.sanitize("<script><script><div>sanitize</div>")

		expect:
		assertTrue(result.isInvalidMarkup())
	}

	def 'testValidateHtmlScriptTagWithErrors'(){
		setup:
		def result = sanitizer.validateMarkup("<script><script><div>sanitize</div>")

		expect:
		assertTrue(result.isInvalidMarkup())
	}
}
