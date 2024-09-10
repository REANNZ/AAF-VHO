<html>
  <head>
    <meta name="layout" content="public" />
  </head>
  
  <body>
    <h2><g:message encodeAs='HTML' code="views.aaf.base.identity.federatedsessions.federatedincomplete.heading"/></h2>

    <div class="alert alert-block alert-error">
      <p><g:message encodeAs='HTML' code="views.aaf.base.identity.federatedsessions.federatedincomplete.details"/></p>
    
      <g:if test="${errors?.size() > 0}">
        <ul>
          <g:each in="${errors}" var="msg">
            <li><g:message encodeAs='HTML' code="${msg}" /></li>
          </g:each>
        </ul>
      </g:if>
    </div>

    <p><g:message code="branding.application.supportdesk"/></p>
    <br><br><br>

    <h4 class="muted"><g:message encodeAs='HTML' code="views.aaf.base.identity.auth.federatederror.details.completedetails"/></h4>
    <g:include controller="auth" action="echo" />

  </body>
</html>
