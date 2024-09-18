<html>
  <head>
    <meta name="layout" content="internal">
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:link action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.apisubject"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:link action="show" id="${apiSubject.id}">${fieldValue(bean: apiSubject, field: "principal")}</g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.apisubject.edit"/></li>
    </ul>

    <g:render template="/templates/flash" />
    <g:render template="/templates/errors_bean" model="['bean':apiSubject]"/>

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.apisubject.edit.heading" args="[apiSubject.principal]"/></h2>

    <g:form action="update" class="form form-horizontal form-validating">
      <fieldset>
        <g:render template="form"/>
      </fieldset>
      <fieldset>
        <div class="form-actions">
          <g:submitButton name="update" value="${message(code: 'label.update')}" class="btn btn-success"/>
          <g:link action="show" id="${apiSubject.id}" class="btn"><g:message encodeAs='HTML' code="label.cancel"/></g:link>
        </div>
      </fieldset>
    </g:form>

  </body>
</html>
