<html>
  <head>
    <meta name="layout" content="internal">
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.apisubject"/></li>
      <li class="pull-right"><strong><g:link action="create"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.apisubject.create"/></g:link></strong></li>
    </ul>

    <g:render template="/templates/flash" />
    
    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.apisubject.list.heading" /></h2>

    <table class="table table-borderless table-sortable">
      <thead>
        <tr>
          <th><g:message encodeAs='HTML' code="label.token" /></th>
          <th><g:message encodeAs='HTML' code="label.email" /></th>
          <th><g:message encodeAs='HTML' code="label.description" /></th>
          <th><g:message encodeAs='HTML' code="label.enabled" /></th>
          <th/>
        </tr>
      </thead>
      <tbody>
      <g:each in="${apiSubjects}" status="i" var="subject">
        <tr>
          <td>${fieldValue(bean: subject, field: "principal")}</td>
          <td>${fieldValue(bean: subject, field: "email")}</td>
          <td>${fieldValue(bean: subject, field: "description")}</td>
          <td>${fieldValue(bean: subject, field: "enabled")}</td>
          <td><g:link action="show" id="${subject.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view"/></g:link>
        </tr>
      </g:each>
      </tbody>
    </table>

  </body>
</html>
