package org.grails.plugins.sanitizer

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

	def 'testValid'() {
		expect:
		"test" == codec.encode("test")
	}

	def 'testInValid'() {
		setup:
		result.errorMessages.add "error1"

		expect:
		"" == codec.encode("test")
	}
}
