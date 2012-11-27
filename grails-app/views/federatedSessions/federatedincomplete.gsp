<html>
  <head>
    <meta name="layout" content="public" />
  </head>
  
  <body>
    <h2><g:message code="views.aaf.base.identity.federatedsessions.federatedincomplete.heading"/></h2>

    <div class="alert alert-block alert-error">
      <p><g:message code="views.aaf.base.identity.federatedsessions.federatedincomplete.details"/></p>
    
      <g:if test="${errors && errors.errorCount > 0}">
        <ul>
          <g:each in="${errors}" var="msg">
            <li><g:message code="${msg}" /></li>
          </g:each>
        </ul>
      </g:if>
    </div>

    <p><g:message code="branding.application.supportdesk"/></p>
    <br><br><br>

    <h4 class="muted">Complete Request Details</h4>
    <g:include controller="auth" action="echo" />

  </body>
</html>