<html>
  <head>
    <meta name="layout" content="internal">
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:link controller="role" action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.role"/></g:link> <span class="divider">/</span></li>
      <li class="active">${fieldValue(bean: role, field: "name")}</li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.role.show.heading" args="[role.name]"/></h2>

    <ul class="nav nav-tabs">
      <li class="active"><a href="#tab-overview" data-toggle="tab"><g:message encodeAs='HTML' code="label.overview" /></a></li>
      <li><a href="#tab-members" data-toggle="tab"><g:message encodeAs='HTML' code="label.members" /></a></li>
      <li><a href="#tab-permissions" data-toggle="tab"><g:message encodeAs='HTML' code="label.permissions" /></a></li>

      <li class="dropdown pull-right">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
          <g:message encodeAs='HTML' code="label.actions" />
          <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
          <li><g:link contoller='role' action='edit' id="$role.id"><g:message encodeAs='HTML' code="label.edit"/></g:link></li>
          <li>
            <a href="#" class="delete-ensure" data-confirm="${message(code:'views.aaf.base.admin.role.confirm.remove')}"><g:message encodeAs='HTML' code="label.delete"/></a>
            <g:form action="delete" method="delete">
              <g:hiddenField name="id" value="${role.id}" />
            </g:form>
          </li>
        </ul>
      </li>
    </ul>

    <div class="tab-content">
      <div id="tab-overview" class="tab-pane active">
        <div id="overview-role">
          <table class="table table-borderless">
            <tbody>
              <tr>
                <th><g:message encodeAs='HTML' code="label.id" /></th>
                <td><g:fieldValue bean="${role}" field="id"/></td>
              </tr>
              <tr>
                <th><g:message encodeAs='HTML' code="label.name" /></th>
                <td><g:fieldValue bean="${role}" field="name"/></td>
              </tr>
              <tr>
                <th><g:message encodeAs='HTML' code="label.description" /></th>
                <td><g:fieldValue bean="${role}" field="description"/></td>
              </tr>
              <tr>
                <th><g:message encodeAs='HTML' code="label.protect" /></th>
                <td><g:formatBoolean boolean="${role?.protect}" /></td>
              </tr>
            </tbody>
          </table>
        </div>
        <div id="editor-role" class="revealable">
          <h3><g:message encodeAs='HTML' code="label.editingrole"/></h3>

          <g:hasErrors bean="${role}">
          <ul class="clean alert alert-error">
            <g:eachError bean="${role}" var="error">
            <li <g:if test="${error in org.springframework.validation.FieldError}">data-field-id="${error.field}"</g:if>><g:message encodeAs='HTML' error="${error}"/></li>
            </g:eachError>
          </ul>
          </g:hasErrors>
          <g:form method="post" class="form form-horizontal">
            <g:hiddenField name="id" value="${role?.id}" />
            <g:hiddenField name="version" value="${role?.version}" />
            <fieldset class="form">
              <g:render template="form"/>
            </fieldset>
            <fieldset>
              <div class="form-actions">
                <g:actionSubmit class="save" action="update" class="btn btn-success" value="${message(code: 'label.update', default: 'Update')}" />
                <a class="cancel-edit-role btn"><g:message encodeAs='HTML' code="label.cancel"  /></a>
              </div>
            </fieldset>
          </g:form>
        </div>
      </div>

      <div id="tab-members" class="tab-pane">
        <g:if test="${role.subjects}">
          <table class="table table-borderless table-sortable">
            <thead>
              <tr>
                <th><g:message encodeAs='HTML' code="label.id" /></th>
                <th><g:message encodeAs='HTML' code="label.name" /></th>
                <th class="hidden-phone"><g:message encodeAs='HTML' code="label.principal" /></th>
                <th/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${role.subjects.sort{it.id}}" var="subject">
                <tr>
                  <td><g:fieldValue bean="${subject}" field="id"/></td>
                  <td><g:fieldValue bean="${subject}" field="cn"/></td>
                  <td class="hidden-phone"><g:fieldValue bean="${subject}" field="principal"/></td>
                  <td class="pull-right">
                    <g:link controller="subject" action="show" id="${subject.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view" /></g:link>
                    <a href="#" class="delete-ensure btn btn-small btn-danger" data-confirm="${message(code:'views.aaf.base.admin.role.member.confirm.remove', args:[subject.cn])}"><g:message encodeAs='HTML' code="label.delete"/></a>
                    <g:form action="removemember" method="post" class="hidden">
                      <g:hiddenField name="id" value="${role?.id}" />
                      <g:hiddenField name="subjectID" value="${subject.id}" />
                    </g:form>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </g:if>
        <g:else>
          <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.admin.role.members.none"/></p>
        </g:else>

        <div class="table-modifiers">
          <a id="show-add-role-members" class="btn btn-info"><g:message encodeAs='HTML' code="label.addmembers"/></a>
          <div id="add-role-members"></div>
        </div>
      </div>

      <div id="tab-permissions" class="tab-pane">
        <g:if test="${role.permissions}">
          <table class="table table-borderless">
            <thead>
              <tr>
                <th class="hidden-phone"><g:message encodeAs='HTML' code="label.type" /></th>
                <th><g:message encodeAs='HTML' code="label.target" /></th>
                <th><g:message encodeAs='HTML' code="label.managed" /></th>
                <th/>
              </tr>
            </thead>
            <tbody>
              <g:each in="${role.permissions.sort{it.id}}" var="perm">
                <tr>
                  <td class="hidden-phone"><g:fieldValue bean="${perm}" field="displayType"/></td>
                  <td><g:fieldValue bean="${perm}" field="target"/></td>
                  <td><g:fieldValue bean="${perm}" field="managed"/></td>
                  <td>
                    <a href="#" data-confirm="${message(code:'views.aaf.base.admin.role.permission.confirm.remove', args:[perm.target])}" class="btn btn-danger btn-small delete-ensure pull-right"><g:message encodeAs='HTML' code="label.delete"/></a>
                    <g:form action="deletepermission" method="post" class="form">
                      <g:hiddenField name="id" value="${role?.id}" />
                      <g:hiddenField name="permID" value="${perm.id}" />
                      <g:hiddenField name="version" value="${role?.version}" />
                    </g:form>
                  </td>
                </tr>
              </g:each>
            </tbody>
          </table>
        </g:if>
        <g:else>
          <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.admin.role.permissions.none"/></p>
        </g:else>

        <g:form method="post" class="form-horizontal form-validating">
          <legend><g:message encodeAs='HTML' code="label.addpermission" /></legend>
          <g:hiddenField name="id" value="${role?.id}" />
          <g:hiddenField name="version" value="${role?.version}" />
          <div class="control-group">
            <label class="control-label"><g:message encodeAs='HTML' code="label.target" /></label>
            <div class="controls">
              <input name="target" class="span4 required" placeholder="target:must:be:colon:seperated:use:*:for:matchall"></input>
            </div>
          </div>
          <div class="form-actions">
            <g:actionSubmit action="createpermission" class="btn btn-success" value="${message(code: 'label.create', default: 'Create')}" />
          </div>
        </g:form>
      </div>
    </div>

    <r:script>
      var searchNewMembersEndpoint = "${createLink(controller:'role', action:'searchNewMembers', id:role.id)}";
    </r:script>
  </body>
</html>
