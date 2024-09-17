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

  shiroAuthenticationStrategy(aaf.base.shiro.FirstExceptionStrategy)
}
