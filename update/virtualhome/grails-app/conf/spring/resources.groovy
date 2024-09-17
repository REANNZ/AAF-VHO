beans = {

  // Port of mulireadFilter from the old web.xml
  filter('multireadFilter', aaf.base.util.http.MultiReadServletFilter) {
    urlPatterns = ['/api/*']
  }

  // Port of charEncodingFilter from the old web.xml
  filter('charEncodingFilter', org.springframework.web.filter.DelegatingFilterProxy) {
    targetBeanName = 'charEncodingFilter'
    targetFilterLifecycle = true
    urlPatterns = ['/*']
  }

  // Port of grailsApplication from applicationContext.xml
  grailsApplication(org.codehaus.groovy.grails.commons.GrailsApplicationFactoryBean) {
      description = "Grails application factory bean"
      grailsDescriptor = "/WEB-INF/grails.xml"
  }

  // Port of grailsConfigurator from applicationContext.xml
  grailsConfigurator(org.codehaus.groovy.grails.commons.spring.GrailsRuntimeConfigurator) {
      pluginManager = ref("pluginManager")
      // Constructor arg
      constructorArgs = [ref("grailsApplication")]
  }

  // Port of characterEncodingFilter from applicationContext.xml
  characterEncodingFilter(org.springframework.web.filter.CharacterEncodingFilter) {
      encoding = "utf-8"
  }

  // Port of pluginManager from applicationContext.sml
  pluginManager(org.codehaus.groovy.grails.plugins.GrailsPluginManagerFactoryBean) {
      description = "A bean that manages Grails plugins"
      grailsDescriptor = "/WEB-INF/grails.xml"
      application = ref("grailsApplication")
  }

  // Port of conversionService from applicationContext.xml
  conversionService(org.springframework.context.support.ConversionServiceFactoryBean)


  shiroAuthenticationStrategy(aaf.base.shiro.FirstExceptionStrategy)
}
