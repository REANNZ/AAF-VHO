
<html>
  <head>
    <meta name="layout" content="internal" />
  </head>
  
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="emailTemplate" action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin.emailtemplate"/></g:link> <span class="divider">/</span></li>
      <li class="active">${fieldValue(bean: emailtemplate, field: "name")}</li>
    </ul>

    <g:render template="/templates/flash" />
    <g:render template="/templates/errors_bean" model="['bean':emailtemplate]"/>

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.emailtemplate.show.heading" args="[emailtemplate.name]"/></h2>

    <ul class="nav nav-tabs">
      <li class="active"><a href="#tab-overview" data-toggle="tab"><g:message encodeAs='HTML' code="label.overview" /></a></li>

      <li class="dropdown pull-right">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
          <g:message encodeAs='HTML' code="label.actions" />
          <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
          <li>
            <g:link controller="emailTemplate" action="edit" id="${emailtemplate.id}"><g:message encodeAs='HTML' code="label.edit"/></g:link>
          </li>
        </ul>
      </li>
    </ul>

    <div class="tab-content">
      <div id="tab-overview" class="tab-pane active">
        <div class="row">
          <div class="span4">
            <table class="table table-borderless">
              <tbody>   
                <tr>
                  <th><g:message encodeAs='HTML' code="label.name" /></th>
                  <td>${fieldValue(bean: emailtemplate, field: "name")}</td>
                </tr>
                <tr>
                  <th><g:message encodeAs='HTML' code="label.created" /></th>
                  <td>${fieldValue(bean: emailtemplate, field: "dateCreated")}</td>
                </tr>
                <tr>
                  <th><g:message encodeAs='HTML' code="label.lastupdated" /></th>
                  <td>${fieldValue(bean: emailtemplate, field: "lastUpdated")}</td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="span8">
            <pre style="padding: 24px;">${fieldValue(bean: emailtemplate, field: "content")}</pre>
          </div>
        </div>
      </div>
    </div>

  </body>
</html>
