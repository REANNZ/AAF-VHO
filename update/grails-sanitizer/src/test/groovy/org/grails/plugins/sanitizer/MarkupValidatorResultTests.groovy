package org.grails.plugins.sanitizer

import grails.test.*

import spock.lang.Specification

class MarkupValidatorResultTests extends Specification {

	def 'test is invalid markup 1'() {
		given:
		def val = new MarkupValidatorResult()

		expect:
		assertFalse(val.isInvalidMarkup())
	}

	def 'test is invalid markup 2'() {
		given:
		def val = new MarkupValidatorResult()
		val.dirtyString = "test"

		expect:
		assertFalse(val.isInvalidMarkup())
	}

	def 'test is invalid markup 3'() {
		given:
		def val = new MarkupValidatorResult()
		val.errorMessages.add("error1")

		expect:
		assertTrue(val.isInvalidMarkup())
	}

	def 'test is invalid markup 4'() {
		given:
		def val = new MarkupValidatorResult()

		expect:
		val.errorMessages.add("error2")
		assertTrue(val.isInvalidMarkup())
	}

	def 'test is invalid markup 5'() {
		given:
		def val = new MarkupValidatorResult()

		expect:
		val.errorMessages.clear()
		assertFalse(val.isInvalidMarkup())

	}

}
