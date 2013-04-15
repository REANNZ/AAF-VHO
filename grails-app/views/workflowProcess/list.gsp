
<html>
  <head>
    <meta name="layout" content="internal" />
  </head>
  <body>
    
    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.workflow.process"/></li>
      
      <li class="pull-right"><strong><g:link controller="workflowProcess" action="create"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.workflow.process.create"/></g:link></strong></li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.workflow.process.list.heading" /></h2>
  
    <table class="table borderless table-sortable">
      <thead>
        <tr>
          <th><g:message encodeAs='HTML' code="label.name" default="Name"/></th>
          <th><g:message encodeAs='HTML' code="label.description" default="Description"/></th>
          <th/>
        </tr>
      </thead>
      <tbody>
      <g:each in="${processList.sort{it.name}}" var="p" status="i">
        <tr>
          <td>${fieldValue(bean: p, field: "name")}</td>
          <td>${fieldValue(bean: p, field: "description")}</td>
          <td>
           <g:link controller='workflowProcess' action='show' id="$p.id" class="btn"><g:message encodeAs='HTML' code="label.view"/></g:link>
          </td>
        </tr>
      </g:each>
      </tbody>
    </table>
  </body>
</html>
