<div class="container">  
  <div class="row">
    <div class="span12">
      <g:img dir="images" file="logo.jpg" alt="${message(code:'branding.application.name')}" width="234" height="82"/>
      <h1><g:message encodeAs='HTML' code='branding.application.name' /></h1> 
      <g:if test="${grails.util.Environment.current == grails.util.Environment.TEST}">
        <h2><g:message encodeAs='HTML' code="branding.application.testfederation.name"/></h2>
      </g:if>
      <g:if test="${grails.util.Environment.current == grails.util.Environment.DEVELOPMENT}">
        <h2><g:message encodeAs='HTML' code="branding.application.development.name"/></h2>
      </g:if>
    </div>
  </div>
</div>
