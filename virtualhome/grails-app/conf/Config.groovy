import javax.naming.InitialContext
import javax.naming.Context

import grails.util.Environment

// Import externalized configuration
if(Environment.current != Environment.TEST) {
  def externalConf = getFromEnvironment("config_dir")
  if(externalConf) {
    grails.config.locations = ["file:${externalConf}/application_config.groovy"]
  } else {
    println "No external configuration location specified as environment variable config_dir, terminating startup"
    throw new RuntimeException("No external configuration location specified as environment variable config_dir")
  }
}

// Extract user details to append to Audit Table
auditLog {
  actorClosure = { request, session ->
    org.apache.shiro.SecurityUtils.getSubject()?.getPrincipal()
  }
}

security.shiro.authc.required = false
security.shiro.authc.strategy = aaf.base.shiro.FirstExceptionStrategy

// Enable console plugin
grails.plugin.console.enabled = true

grails.project.groupId = appName
grails.converters.xml.pretty.print = true

grails.scaffolding.templates.domainSuffix = 'Instance'
grails.resources.adhoc.patterns = ['/images/*', '/css/*', '/js/*', '/plugins/*']

grails.json.legacy.builder = false
grails.enable.native2ascii = true
grails.spring.bean.packages = []
grails.web.disable.multipart=false

grails.exceptionresolver.params.exclude = ['password', 'password_confim']

environments {
  test {
    testDataConfig.enabled = true
    
    grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
    grails.mail.default.from="noreply-test@aaf.edu.au"
    greenmail.disabled = false
  }
}

/**
* This is allows usage of environment variables in production
* while maintaining flexibility in development.
*/
public String getFromEnvironment(final String name) {
  if(name == null) return null;
  try {
    final Object object = ((Context)(new InitialContext().lookup("java:comp/env"))).lookup(name);
    if (object != null)
      return object.toString();
  } catch (final Exception e) {}

  System.getenv(name);
}

