<html>
  <head>
    <meta name="layout" content="internal">
  </head>
  <body>
    
    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role"/></li>
      <li class="pull-right"><strong><g:link controller="role" action="create"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role.create"/></g:link></strong></li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.role.list.heading"/></h2>

    <div class="row">
      <div class="span12">
        <table class="table table-borderless table-sortable">
          <thead>
            <tr>
              <th><g:message encodeAs='HTML' code="role.name.label" default="Name" /></th>
              <th><g:message encodeAs='HTML' code="role.description.label" default="Description" /></th>
              <th><g:message encodeAs='HTML' code="role.protect.label" default="Protect" /></th>
              <th/>
            </tr>
          </thead>
          <tbody>
          <g:each in="${roles}" status="i" var="role">
            <tr>
              <td>${fieldValue(bean: role, field: "name")}</td>
              <td>${fieldValue(bean: role, field: "description")}</td>
              <td><g:formatBoolean boolean="${role.protect}" /></td>
              <td><g:link action="show" id="${role.id}" class="btn btn-small pull-right"><g:message encodeAs='HTML' code="label.view"/></g:link></td>
            </tr>
          </g:each>
          </tbody>
        </table>
      </div>
    </div>

  </body>
</html>
