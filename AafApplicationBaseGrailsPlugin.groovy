import org.codehaus.groovy.grails.commons.GrailsApplication
import org.apache.shiro.SecurityUtils
import aaf.base.identity.Subject

class AafApplicationBaseGrailsPlugin {

  def version = "0.2"

  def grailsVersion = "2.1 > *"

  def dependsOn = [:]

  def pluginExcludes = [
  "grails-app/views/error.gsp"
  ]

  def title = "AAF Application Base"
  def author = "Bradley Beddoes"
  def authorEmail = "bradleybeddoes@aaf.edu.au"
  def description = '''\
  Base environment for all AAF applications to build from
  '''

  def documentation = "http://www.aaf.edu.au"

  def doWithWebDescriptor = { xml ->
  }

  def doWithSpring = {
  }

  def doWithDynamicMethods = { ctx ->
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

  def doWithApplicationContext = { applicationContext ->
  }

  def onChange = { event ->
    injectAuthn(event.source)
  }

  def onConfigChange = { event ->
  }

  def onShutdown = { event ->
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