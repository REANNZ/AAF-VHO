beans = {
    // Grails Application Factory Bean
    grailsApplication(org.codehaus.groovy.grails.commons.GrailsApplicationFactoryBean) {
        description = "Grails application factory bean"
        grailsDescriptor = "/WEB-INF/grails.xml"
    }

    // Plugin Manager Bean
    pluginManager(org.codehaus.groovy.grails.plugins.GrailsPluginManagerFactoryBean) {
        description = "A bean that manages Grails plugins"
        grailsDescriptor = "/WEB-INF/grails.xml"
        application = ref("grailsApplication")
    }

    // Grails Runtime Configurator Bean
    grailsConfigurator(org.codehaus.groovy.grails.commons.spring.GrailsRuntimeConfigurator) {
        pluginManager = ref("pluginManager")
        // Constructor arg
        constructorArgs = [ref("grailsApplication")]
    }

    // Character Encoding Filter Bean
    characterEncodingFilter(org.springframework.web.filter.CharacterEncodingFilter) {
        encoding = "utf-8"
    }

    // Conversion Service Bean
    conversionService(org.springframework.context.support.ConversionServiceFactoryBean)
}