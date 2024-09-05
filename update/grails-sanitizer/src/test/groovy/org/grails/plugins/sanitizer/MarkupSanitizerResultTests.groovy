package org.grails.plugins.sanitizer

import grails.test.*

import spock.lang.Specification

class MarkupSanitizerResultTests extends Specification {

	def 'test special constructor'() {
		def vResult = new MarkupValidatorResult()
		vResult.dirtyString = "vTest"
		vResult.errorMessages.add "vError1"

		MarkupSanitizerResult sResult = new MarkupSanitizerResult(vResult)
		assertEquals("vTest", sResult.dirtyString)
		assertEquals("vError1", sResult.errorMessages[0])
	}

	def 'simple property test'() {
		def vResult = new MarkupSanitizerResult()
		MarkupValidatorResultTests.simplePropertyTests vResult
	}
}
