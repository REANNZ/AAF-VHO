import org.springframework.web.filter.CharacterEncodingFilter
import org.springframework.context.support.ConversionServiceFactoryBean
import org.springframework.boot.web.servlet.FilterRegistrationBean

import aaf.base.shiro.FirstExceptionStrategy

beans = {

  // Port of characterEncodingFilter from applicationContext.xml
  characterEncodingFilter(CharacterEncodingFilter) {
    encoding = 'utf-8'
  }

  // Port of conversionService from applicationContext.xml
  conversionService(ConversionServiceFactoryBean)

  shiroAuthenticationStrategy(FirstExceptionStrategy)

  // Replaces the old actorClosure from Config.groovy.
  // The 'grails-audit-logging' plugin, of course, had an overhaul.
  auditRequestResolver(ShiroAuditResolver) {
    //customService = ref('customService')
  }

  // The following has yet to be converted (if it's even applicable)
  /*
    <filter>
      <filter-name>charEncodingFilter</filter-name>
      <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
      <init-param>
          <param-name>targetBeanName</param-name>
          <param-value>characterEncodingFilter</param-value>
      </init-param>
      <init-param>
          <param-name>targetFilterLifecycle</param-name>
          <param-value>true</param-value>
      </init-param>
    </filter>


    <filter>
      <filter-name>multireadFilter</filter-name>
      <filter-class>aaf.base.util.http.MultiReadServletFilter</filter-class>
    </filter>

    <filter>
        <filter-name>charEncodingFilter</filter-name>
        <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
        <init-param>
            <param-name>targetBeanName</param-name>
            <param-value>characterEncodingFilter</param-value>
        </init-param>
        <init-param>
            <param-name>targetFilterLifecycle</param-name>
            <param-value>true</param-value>
        </init-param>
    </filter>

  */
}
