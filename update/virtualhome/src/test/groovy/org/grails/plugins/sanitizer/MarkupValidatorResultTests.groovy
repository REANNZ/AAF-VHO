package org.grails.plugins.sanitizer

import spock.lang.*

class MarkupValidatorResultTests extends Specification {

	def 'testIsInvalidMarkup'() {
		setup:
		def val = new MarkupValidatorResult()

		expect:
		assertFalse(val.isInvalidMarkup())
	}

	def 'testIsInvalidMarkup'() {
		setup:
		def val = new MarkupValidatorResult()

		when:
		val.dirtyString = "test"

		then:
		assertFalse(val.isInvalidMarkup())

		val.errorMessages.add("error1")

		assertTrue(val.isInvalidMarkup())

		val.errorMessages.add("error2")

		assertTrue(val.isInvalidMarkup())

		val.errorMessages.clear()

		assertFalse(val.isInvalidMarkup())
	}

	def 'testIsInvalidMarkup'() {
		setup:
		def val = new MarkupValidatorResult()

		when:
		val.dirtyString = "test"
		val.errorMessages.add("error1")

		then:
		assertTrue(val.isInvalidMarkup())

		val.errorMessages.add("error2")

		assertTrue(val.isInvalidMarkup())

		val.errorMessages.clear()

		assertFalse(val.isInvalidMarkup())
	}

	def 'testIsInvalidMarkup'() {
		setup:
		def val = new MarkupValidatorResult()

		when:
		val.dirtyString = "test"
		val.errorMessages.add("error1")
		val.errorMessages.add("error2")

		then:
		assertTrue(val.isInvalidMarkup())

		val.errorMessages.clear()

		assertFalse(val.isInvalidMarkup())
	}

	def 'testIsInvalidMarkup'() {
		setup:
		def val = new MarkupValidatorResult()

		when:
		val.dirtyString = "test"
		val.errorMessages.add("error1")
		val.errorMessages.add("error2")
		val.errorMessages.clear()

		then:
		assertFalse(val.isInvalidMarkup())
	}
}
