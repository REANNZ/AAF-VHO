<html>
  <head>
    <meta name="layout" content="internal" />  
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li><g:link action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.apisubject"/></g:link> <span class="divider">/</span></li>
      <li class="active">${fieldValue(bean: apiSubject, field: "principal")}</li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.apisubject.show.heading" args="[apiSubject.principal]"/></h2>

    <ul class="nav nav-tabs">
      <li class="active"><a href="#tab-overview" data-toggle="tab"><g:message encodeAs='HTML' code="label.overview"  /></a></li>
      <li><a href="#tab-roles" data-toggle="tab"><g:message encodeAs='HTML' code="label.roles" /></a></li>
      <li><a href="#tab-permissions" data-toggle="tab"><g:message encodeAs='HTML' code="label.permissions"  /></a></li>

      <li class="dropdown pull-right">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
          <g:message encodeAs='HTML' code="label.actions" />
          <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
          <li><g:link action="edit" id="${apiSubject.id}"><g:message encodeAs='HTML' code="label.edit"/></g:link></li>
          <g:if test="${apiSubject.enabled}">
            <li>
              <a href="#" class="form-link-submitter"><g:message encodeAs='HTML' code="label.disable"/></a>
              <g:form action="disablesubject">
                <g:hiddenField name="id" value="${apiSubject?.id}" />
              </g:form>
            </li>
          </g:if>
          <g:else>
            <li>
              <a href="#" class="form-link-submitter"><g:message encodeAs='HTML' code="label.enable"/></a>
              <g:form action="enablesubject">
                <g:hiddenField name="id" value="${apiSubject?.id}" />
              </g:form>
            </li>
          </g:else>
          <li class="divider"></li>
          <li>
            <a href="#" class="delete-ensure" data-confirm="${message(code:'views.aaf.base.admin.apisubject.confirm.remove')}"><g:message encodeAs='HTML' code="label.delete"/></a>
            <g:form action="delete" method="delete">
              <g:hiddenField name="id" value="${apiSubject.id}" />
            </g:form>
          </li>
        </ul>
      </li>
    </ul>

    <div class="tab-content">
      <div id="tab-overview" class="tab-pane active">
        <table class="table table-borderless fixed">
          <tbody>
            <tr>
              <th><g:message encodeAs='HTML' code="label.token" /></th>
              <td><code>${fieldValue(bean: apiSubject, field: "principal")}</code></td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.secret" /></th>
              <td><code>${fieldValue(bean: apiSubject, field: "apiKey")}</code></td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.email" /></th>
              <td>${fieldValue(bean: apiSubject, field: "email")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.description" /></th>
              <td>${fieldValue(bean: apiSubject, field: "description")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.enabled" /></th>
              <td>${formatBoolean(boolean: apiSubject.enabled)}</td>
            </tr>

            <tr><td colspan="2"><hr></tr></td>
            <tr><td colspan="2"><strong><g:message encodeAs='HTML' code="label.internaldata"/></strong></td></tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.id" /></th>
              <td>${fieldValue(bean: apiSubject, field: "id")}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div id="tab-roles" class="tab-pane">
        <g:if test="$apiSubject.roles}">
          <table class="table table-borderless table-sortable">
            <thead>
              <tr>
                <th><g:message encodeAs='HTML' code="label.name" /></th>
                <th><g:message encodeAs='HTML' code="label.description" /></th>
                <th/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${apiSubject.roles}" status="i" var="role">
                <tr>
                  <td>${fieldValue(bean: role, field: "name")}</td>
                  <td>${fieldValue(bean: role, field: "description")}</td>
                  <td class="pull-right"><g:link controller="role" action="show" id="${role.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view" /></g:link></td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </g:if>
        <g:else>
          <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.admin.apisubject.show.noroles" /></p>
        </g:else>
      </div>

      <div id="tab-permissions" class="tab-pane">
        <g:if test="${apiSubject.permissions}">
          <table class="table table-borderless">
            <thead>
              <tr>
                <th class="hidden-phone"><g:message encodeAs='HTML' code="label.type" /></th>
                <th><g:message encodeAs='HTML' code="label.target" /></th>
                <th/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${apiSubject.permissions}" status="i" var="perm">
                <tr>
                  <td class="hidden-phone">${fieldValue(bean: perm, field: "displayType")}</td>
                  <td>${fieldValue(bean: perm, field: "target")}</td>
                  <td>
                    <a href="#" data-confirm="${message(code:'views.aaf.base.admin.apisubject.permission.confirm.remove', args:[perm.target])}" class="btn btn-danger btn-small delete-ensure pull-right"><g:message encodeAs='HTML' code="label.delete"/></a>
                    <g:form action="deletepermission" method="post" class="hidden">
                      <g:hiddenField name="id" value="${apiSubject?.id}" />
                      <g:hiddenField name="permID" value="${perm.id}" />
                    </g:form>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </g:if>
        <g:else>
          <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.admin.apisubject.show.nopermissions" /></p>
        </g:else>

        <g:form method="post" class="form-horizontal form-validating">
          <legend><g:message encodeAs='HTML' code="label.addpermission" /></legend>
          <g:hiddenField name="id" value="${apiSubject?.id}" />
          <g:hiddenField name="version" value="${subject?.version}" />
          <div class="control-group">
            <label class="control-label"><g:message encodeAs='HTML' code="label.target" /></label>
            <div class="controls">
              <input type="text" name="target" class="span4" required="required" data-msg-required="${message(code: 'validation.permission.required')}" placeholder="target:must:be:colon:seperated. Use :* to match all"></input>
            </div>
          </div>
          <div class="form-actions">
            <g:actionSubmit action="createpermission" class="btn btn-success" value="${message(code: 'label.create')}" />
          </div>
        </g:form>
      </div>
    </div>

  </body>
</html>
