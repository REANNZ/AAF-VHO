<html>
  <head>
    <meta name="layout" content="internal" />  
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="subject" action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.subject"/></g:link> <span class="divider">/</span></li>
      <li class="active">${fieldValue(bean: subject, field: "cn")}</li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.subject.show.heading" args="[subject.cn]"/></h2>

    <ul class="nav nav-tabs">
      <li class="active"><a href="#tab-overview" data-toggle="tab"><g:message encodeAs='HTML' code="label.overview"  /></a></li>
      <li><a href="#tab-roles" data-toggle="tab"><g:message encodeAs='HTML' code="label.roles" /></a></li>
      <li><a href="#tab-permissions" data-toggle="tab"><g:message encodeAs='HTML' code="label.permissions"  /></a></li>
      <li><a href="#tab-sessions" data-toggle="tab"><g:message encodeAs='HTML' code="label.sessions" /></a></li>

      <li class="dropdown pull-right">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
          <g:message encodeAs='HTML' code="label.actions" />
          <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
          <g:if test="${subject.enabled}">
            <li>
              <a href="#" class="form-link-submitter"><g:message encodeAs='HTML' code="label.disable"/></a>
              <g:form action="disablesubject">
                <g:hiddenField name="id" value="${subject?.id}" />
              </g:form>
            </li>
          </g:if>
          <g:else>
            <li>
              <a href="#" class="form-link-submitter"><g:message encodeAs='HTML' code="label.enable"/></a>
              <g:form action="enablesubject">
                <g:hiddenField name="id" value="${subject?.id}" />
              </g:form>
            </li>
          </g:else>
        </ul>
      </li>
    </ul>

    <div class="tab-content">
      <div id="tab-overview" class="tab-pane active">
        <table class="table table-borderless fixed">
          <tbody>
            <tr>
              <th><g:message encodeAs='HTML' code="label.id" /></th>
              <td>${fieldValue(bean: subject, field: "id")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.principal" /></th>
              <td>${fieldValue(bean: subject, field: "principal")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.cn" /></th>
              <td>${fieldValue(bean: subject, field: "cn")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.email" /></th>
              <td>${fieldValue(bean: subject, field: "email")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.sharedtoken" /></th>
              <td>${fieldValue(bean: subject, field: "sharedToken")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.enabled" /></th>
              <td>${formatBoolean(boolean: subject.enabled)}</td>
            </tr>
          </tbody>
        </table>
      </div>

      <div id="tab-roles" class="tab-pane">
        <g:if test="${subject.roles}">
          <table class="table table-borderless table-sortable">
            <thead>
              <tr>
                <th><g:message encodeAs='HTML' code="label.name" /></th>
                <th><g:message encodeAs='HTML' code="label.description" /></th>
                <th/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${subject.roles}" status="i" var="role">
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
          <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.admin.subject.show.noroles" /></p>
        </g:else>
      </div>

      <div id="tab-permissions" class="tab-pane">
        <g:if test="${subject.permissions}">
          <table class="table table-borderless">
            <thead>
              <tr>
                <th class="hidden-phone"><g:message encodeAs='HTML' code="label.type" /></th>
                <th><g:message encodeAs='HTML' code="label.target" /></th>
                <th/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${subject.permissions}" status="i" var="perm">
                <tr>
                  <td class="hidden-phone">${fieldValue(bean: perm, field: "displayType")}</td>
                  <td>${fieldValue(bean: perm, field: "target")}</td>
                  <td>
                    <a href="#" data-confirm="${message(code:'views.aaf.base.admin.subject.permission.confirm.remove', args:[perm.target])}" class="btn btn-danger btn-small delete-ensure pull-right"><g:message encodeAs='HTML' code="label.delete"/></a>
                    <g:form action="deletepermission" method="post" class="hidden">
                      <g:hiddenField name="id" value="${subject?.id}" />
                      <g:hiddenField name="permID" value="${perm.id}" />
                    </g:form>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </g:if>
        <g:else>
          <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.admin.subject.show.nopermissions" /></p>
        </g:else>

        <g:form method="post" class="form-horizontal form-validating">
          <legend><g:message encodeAs='HTML' code="label.addpermission" /></legend>
          <g:hiddenField name="id" value="${subject?.id}" />
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

      <div id="tab-sessions" class="tab-pane">
        <table class="table table-borderless table-sortable">
          <thead>
            <tr>
              <th><g:message encodeAs='HTML' code="label.credential" /></th>
              <th><g:message encodeAs='HTML' code="label.remotehost" /></th>
              <th class="hidden-phone"><g:message encodeAs='HTML' code="label.useragent" /></th>
              <th><g:message encodeAs='HTML' code="label.datecreated" /></th>
            </tr>
          </thead>
          <tbody>
            <g:each in="${subject.sessionRecords.sort{it.dateCreated}.reverse()}" status="i" var="session">
              <tr  >
                <td>${fieldValue(bean: session, field: "credential")}</td>
                <td>${fieldValue(bean: session, field: "remoteHost")}</td>
                <td class="hidden-phone">${fieldValue(bean: session, field: "userAgent")}</td>
                <td>${fieldValue(bean: session, field: "dateCreated")}</td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>

  </body>
</html>
