package applicationbase

import grails.plugins.*

import aaf.base.SMSDeliveryService
import aaf.base.identity.Subject
import org.apache.shiro.SecurityUtils

class ApplicationbaseGrailsPlugin extends Plugin {

    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "3.3.8 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    // TODO Fill in these fields
    def title = "REANNZ Application Base" // Headline display name of the plugin
    def author = "Jamie Getty"
    def authorEmail = "jamie.getty@reannz.co.nz"
    def description = '''\
Collection of code required to make the virtualhome application run
'''
    def profiles = ['web']

    // URL to the plugin's documentation
    def documentation = "http://grails.org/plugin/applicationbase"

    def watchedResources = ["file:./grails-app/**/services/*Service.groovy", "file:./grails-app/controllers/**/*Controller.groovy"]

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

    Closure doWithSpring() {
        smsDeliveryService(SMSDeliveryService) {
            it.autowire = 'byName'
        }
    }

    void doWithDynamicMethods() {
        // Supply authenticated subject to filters
        application.filtersClasses.each { filter ->
            // Should be used after verified call to 'accessControl' 
            injectAuthn(filter.clazz)      
        }

        // Supply authenticated subject to controllers
        application.controllerClasses?.each { controller ->
            injectAuthn(controller.clazz)
        }

        // Supply authenticated subject to services
        application.serviceClasses?.each { service ->
            injectAuthn(service.clazz)
        }
  }

    void doWithApplicationContext() {
        // TODO Implement post initialization spring config (optional)
    }

    void onChange(Map<String, Object> event) {
        injectAuthn(event.source)
    }

    void onConfigChange(Map<String, Object> event) {
        // TODO Implement code that is executed when the project configuration changes.
        // The event is the same as for 'onChange'.
    }

    void onShutdown(Map<String, Object> event) {
        // TODO Implement code that is executed when the application shuts down (optional)
    }

    // Inject the authenticated Subject object
    private void injectAuthn(def clazz) {
        clazz.metaClass.getPrincipal = {
        def subject = SecurityUtils.getSubject()
        }
        
        clazz.metaClass.getSubject = {
        def subject = null
        def principal = SecurityUtils.subject?.principal

        if(principal) {
            subject = aaf.base.identity.Subject.get(principal)
            log.debug "returning $subject"
        }
        subject
        }
    }
}
