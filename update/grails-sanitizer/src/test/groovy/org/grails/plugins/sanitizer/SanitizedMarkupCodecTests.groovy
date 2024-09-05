package org.grails.plugins.sanitizer

import grails.test.*

import spock.lang.*

class SanitizedMarkupCodecTests extends Specification {

	@Shared def service
	@Shared def codec
	@Shared def result

	def setup() {
		codec = new SanitizedMarkupCodec()
		result = new MarkupSanitizerResult()

		service = [sanitize: { result }]
		codec.markupSanitizerService = service

		result.dirtyString = "test"
		result.cleanString = "test"
	}

	def 'test valid input'() {
		expect:
		assertEquals "test", codec.encode("test")
	}

	def 'test invalid input'() {
		given:
		result.errorMessages.add "error1"

		expect:
		assertEquals "", codec.encode("test")
	}
}
