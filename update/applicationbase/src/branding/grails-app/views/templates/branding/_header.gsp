<div class="container">  
  <div class="row">
    <div class="span12">
      <r:img dir='images' file='logo.jpg' plugin="aafApplicationBase" alt="${message(code:'branding.application.name')}" width="234" height="82" />
      <h1><g:message encodeAs='HTML' code='branding.application.name' /></h1> 
      <g:if test="${grailsApplication.config.env == 'test'}">
        <h2><g:message encodeAs='HTML' code="branding.application.testfederation.name"/></h2>
      </g:if>
      <g:if test="${grailsApplication.config.env == 'development'}">
        <h2><g:message encodeAs='HTML' code="branding.application.development.name"/></h2>
      </g:if>
    </div>
  </div>
</div>
