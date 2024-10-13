package org.grails.plugins.sanitizer

import grails.test.*

import grails.util.Holders

class SanitizedMarkupCodecIntegrationTests extends GroovyTestCase {

	void testHappyPath() {
		String my = "<p>my paragraph</p>"

		assertEquals("<p>my paragraph</p>", my.encodeAsSanitizedMarkup())
	}

	void testTrustedSanitizerFixTag() {
		Holders.getConfig().sanitizer.trustSanitizer = true

		String my = "<p>my paragraph<p>"
		assertEquals("<p>my paragraph</p>", my.encodeAsSanitizedMarkup())
	}

	void testTrustedSanitizerStripScript() {
		Holders.getConfig().sanitizer.trustSanitizer = true

		String my = "<script></script><p>my paragraph</p>"
		assertEquals("<p>my paragraph</p>", my.encodeAsSanitizedMarkup())
	}
}
