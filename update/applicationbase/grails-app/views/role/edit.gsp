<html>
  <head>
    <meta name="layout" content="internal">
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:link controller="role" action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:link controller="role" action="show" id="${role.id}">${fieldValue(bean: role, field: "name")}</g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role.edit"/></li>
    </ul>

    <g:render template="/templates/flash" />
    <g:render template="/templates/errors_bean" model="['bean':role]"/>

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.role.edit.heading" args="[role.name]"/></h2>

    <g:form action="update" class="form form-horizontal validating">
      <fieldset>
        <g:render template="form"/>
      </fieldset>
      <fieldset>
        <div class="form-actions">
          <g:submitButton name="update" value="${message(code: 'label.update')}" class="btn btn-success"/>
          <g:link action="show" id="${role.id}" class="btn"><g:message encodeAs='HTML' code="label.cancel"/></g:link>
        </div>
      </fieldset>
    </g:form>

  </body>
</html>
