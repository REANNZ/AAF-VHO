<aaf:isLoggedIn>
  <r:script>
    var pollURL = "${createLink(controller:'auth', action:'poll')}"
    var logoutURL = "${createLink(controller:'auth', action:'logout')}"
    aaf_base.sessionTimeoutMonitor(${grailsApplication.config.aaf.base.session_warning} * 60 * 1000, ${grailsApplication.config.aaf.base.session_decision_time} * 60 * 1000, "${message(code:'templates.session_check.message')}", pollURL, logoutURL);        
  </r:script>
</aaf:isLoggedIn>
