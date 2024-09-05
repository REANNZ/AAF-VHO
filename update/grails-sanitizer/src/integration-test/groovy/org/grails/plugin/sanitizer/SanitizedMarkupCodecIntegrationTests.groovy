package org.grails.plugins.sanitizer

import grails.testing.mixin.integration.Integration
import grails.gorm.transactions.*
import spock.lang.*

import grails.util.Holders

@Integration
@Rollback
class SanitizedMarkupCodecIntegrationTests extends Specification {

	void testHappyPath() {
		String my = "<p>my paragraph</p>"

		assertEquals("<p>my paragraph</p>", my.encodeAsSanitizedMarkup())
	}

	void testTrustedSanitizerFixTag() {
		Holders.config.sanitizer = [trustSanitizer: true]

		String my = "<p>my paragraph<p>"
		assertEquals("<p>my paragraph</p>", my.encodeAsSanitizedMarkup())
	}

	void testTrustedSanitizerStripScript() {
		Holders.config.sanitizer = [trustSanitizer: true]

		String my = "<script></script><p>my paragraph</p>"
		assertEquals("<p>my paragraph</p>", my.encodeAsSanitizedMarkup())
	}
}
