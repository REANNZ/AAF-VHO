<html>
  <head>
    <meta name="layout" content="internal">
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:link controller="role" action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role.create"/></li>
    </ul>

    <g:render template="/templates/flash" />
    <g:render template="/templates/errors_bean" model="['bean':role]"/>

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.role.create.heading"/></h2>

    <g:form action="save" class="form form-horizontal validating">
      <fieldset>
        <g:render template="form"/>
      </fieldset>
      <fieldset>
        <div class="form-actions">
          <g:submitButton name="save" value="${message(code: 'label.create')}" class="btn btn-success"/>
          <g:link action="list" class="btn"><g:message encodeAs='HTML' code="label.cancel"/></g:link>
        </div>
      </fieldset>
    </g:form>

  </body>
</html>
