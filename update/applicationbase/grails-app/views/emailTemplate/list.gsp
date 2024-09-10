
<html>
  <head>  
    <meta name="layout" content="internal" />
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin.emailtemplate"/></li>
      
      <li class="pull-right"><strong><g:link controller="emailTemplate" action="create"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin.emailtemplate.create"/></g:link></strong></li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.emailtemplate.list.heading" /></h2>
    
      <table class="table table-borderless table-sortable">
        <thead>
          <tr>
            <th><g:message encodeAs='HTML' code="label.name" default="Name"/></th>
            <th/>
          </tr>
        </thead>
        <tbody>
        <g:each in="${emailtemplateList}" var="p" status="i">
          <tr class="${(i % 2) == 0 ? 'odd' : 'even'}">
            <td>${fieldValue(bean: p, field: "name")}</td>
            <td>
              <g:link controller='emailTemplate' action='show' id="$p.id" class="btn"><g:message encodeAs='HTML' code="label.view"/></g:link>
            </td>
          </tr>
        </g:each>
        </tbody>
      </table>
  </body>
</html>
