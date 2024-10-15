package virtualhome

import grails.boot.GrailsApp
import grails.boot.config.GrailsAutoConfiguration

import org.grails.web.pages.GroovyPagesServlet
import org.springframework.boot.web.servlet.ServletRegistrationBean
import org.springframework.context.annotation.Bean
import org.grails.web.servlet.mvc.GrailsDispatcherServlet

class Application extends GrailsAutoConfiguration {
    static void main(String[] args) {
        GrailsApp.run(Application, args)
    }

    // Port of the gsp servlet from web.xml
    @Bean
    public ServletRegistrationBean gsp() {
        ServletRegistrationBean reg = new ServletRegistrationBean(new GroovyPagesServlet())
        reg.addUrlMappings("*.gsp")
        return reg
    }

    /*@Bean
    public ServletRegistrationBean grails() {
        GrailsDispatcherServlet gds = new GrailsDispatcherServlet()
        gds.setDispatchOptionsRequest(true)

        ServletRegistrationBean reg = new ServletRegistrationBean(gds)
        reg.setLoadOnStartup(1)
        reg.setAsyncSupported(true)
        return reg;
    }*/
}