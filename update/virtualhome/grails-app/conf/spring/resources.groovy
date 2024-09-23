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
}
