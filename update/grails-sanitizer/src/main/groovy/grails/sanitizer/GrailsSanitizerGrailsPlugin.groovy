package grails.sanitizer

import grails.plugins.*

// NOTE: This looks like something from the web but it's a local file
import org.codehaus.groovy.grails.validation.ApplicationContextAwareConstraintFactory

class GrailsSanitizerGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.8 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "Grails Sanitizer" // Headline display name of the plugin
    def author = "Daniel Bower and Jamie Getty"
    def authorEmail = "daniel@bowerstudios.com"
    def description =  '''\
Plugin for Sanitizing Markup(HTML, XHTML, CSS) using OWASP AntiSamy.
Filters malicious content from User generated content (such as that entered through Rich Text boxes).

Features -
* Ruleset in web-app/WEB-INF/antisamy-policy.xml
* Constraint "markup"
  - can be added to domain/command classes to validate that a string is valid and safe markup
  - important note:  The constraint is for validation only, it does not sanitize the string
* Encoding-only Codec "myText.encodeAsSanitizedMarkup()"
  - use the codec or the service to sanitize the string
  - (the codec uses the service, too)
* MarkupSanitizerService
  - use the codec or the service to sanitize the string
  - access in your controllers/services via
    	def markupSanitizerService
  - method MarkupSanitizerResult sanitize(String dirtyString)
  - method MarkupValidatorResult validateMarkup(String htmlString)
  - effectively a singleton, which means the ruleset only needs to be read once on startup

Please note the beta nature of the version number.  This plugin has not been extensively tested.  Please feel
free to send me any results of any testing you may do.

This module does not sanitize a string that does not contain valid markup.  If it does not contain
valid markup, it will simply return an empty string.
'''

    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/grails-sanitizer"

    // Extra (optional) plugin metadata

    // License: one of 'APACHE', 'GPL2', 'GPL3'
//    def license = "APACHE"

    // Details of company behind the plugin (if there is one)
//    def organization = [ name: "My Company", url: "http://www.my-company.com/" ]

    // Any additional developers beyond the author specified above.
//    def developers = [ [ name: "Joe Bloggs", email: "joe@bloggs.net" ]]

    // Location of the plugin's issue tracker.
//    def issueManagement = [ system: "JIRA", url: "http://jira.grails.org/browse/GPMYPLUGIN" ]

    // Online location of the plugin's browseable source code.
//    def scm = [ url: "http://svn.codehaus.org/grails-plugins/" ]

    Closure doWithSpring() { {->
            // TODO Implement runtime spring config (optional)
        }
    }

    void doWithDynamicMethods() {
        // TODO Implement registering dynamic methods to classes (optional)
    }

    void doWithApplicationContext() {
		// Implement post initialization spring config (optional)
		def factory = new ApplicationContextAwareConstraintFactory(
			applicationContext, MarkupConstraint, ["markupSanitizerService"])
		ConstrainedProperty.registerNewConstraint(MarkupConstraint.MARKUP_CONSTRAINT, factory)
    }

    void onChange(Map<String, Object> event) {
        // TODO Implement code that is executed when any artefact that this plugin is
        // watching is modified and reloaded. The event contains: event.source,
        // event.application, event.manager, event.ctx, and event.plugin.
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }
}
