package org.grails.plugins.sanitizer

import spock.lang.*

class MarkupSanitizerResultTests extends Specification {

	def 'testSpecialConstructor'() {
		setup:
		def vResult = new MarkupValidatorResult()
		vResult.dirtyString = "vTest"
		vResult.errorMessages.add "vError1"
		def sResult = new MarkupSanitizerResult(vResult)

		expect:
		assertEquals("vTest", sResult.dirtyString)
		assertEquals("vError1", sResult.errorMessages[0])
	}

	def 'testSimplePropertyTest'() {
		def vResult = new MarkupSanitizerResult()
		MarkupValidatorResultTests.simplePropertyTests vResult
	}
}
