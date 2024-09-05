package org.grails.plugins.sanitizer

import grails.test.*

import org.springframework.core.io.FileSystemResource

import spock.lang.Specification

class AntiSamyMarkupSanitizerTests extends Specification {

	private AntiSamyMarkupSanitizer sanitizer

	def setup() {
		sanitizer = new AntiSamyMarkupSanitizer(
			new FileSystemResource("scripts/antisamyConfigs/antisamy-myspace-1.4.4.xml"))
	}

	/**
	 * Simple helper method for sanitize tests
	 * @param expectation
	 * @param testString
	 */
	void assertSanitized(String expectation, String testString){
		assertEquals(expectation, sanitizer.sanitize(testString).cleanString)
	}

	/**
	 * Simple helper method for validate tests
	 * @param expectation
	 * @param testString
	 */
	void assertValidTrue(String testString){
		assertFalse(sanitizer.validateMarkup(testString).isInvalidMarkup())
	}

	def 'test sanitize sanity'(){
		assertSanitized("sanitize", "sanitize")
	}

	def 'validate sanity'(){
		assertValidTrue("sanitize")
	}

	def 'sanitize HTML'(){
		assertSanitized("<div>sanitize</div>", "<div>sanitize</div>")
	}

	def 'validate HTML'(){
		assertValidTrue("<div>sanitize</div>")
	}

	def 'sanitize HTML script tag'(){
		assertSanitized("<div>sanitize</div>", "<script></script><div>sanitize</div>")
	}

	def 'sanitize HTML script tag with errors'(){
		MarkupSanitizerResult result = sanitizer.sanitize("<script><script><div>sanitize</div>")
		assertTrue(result.isInvalidMarkup())
	}

	def 'validate HTML script tag with errors'(){
		MarkupValidatorResult result = sanitizer.validateMarkup("<script><script><div>sanitize</div>")
		assertTrue(result.isInvalidMarkup())
	}
}
