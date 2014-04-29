<div id="${tab}" class="tab-pane ${cssclass}">
  <g:if test="${collection.size() > 0}">

      <table class="table table-borderless table-sortable">
        <thead>
          <tr>
              <th><g:message encodeAs='HTML' code="label.login" /></th>
              <th><g:message encodeAs='HTML' code="label.cn" /></th>
              <th><g:message encodeAs='HTML' code="label.email" /></th>
              <th/>
          </tr>
        </thead>
        <tbody>
        <g:each in="${collection}" status="i" var="managedSubjectInstance">
          <tr class="${(i % 2) == 0 ? 'even' : 'odd'}">
            <td>${fieldValue(bean: managedSubjectInstance, field: "login")}</td>
            <td>${fieldValue(bean: managedSubjectInstance, field: "cn")}</td>
            <td>${fieldValue(bean: managedSubjectInstance, field: "email")}</td>
            <td>
              <aaf:hasPermission target="app:manage:organization:${managedSubjectInstance.organization.id}:group:${managedSubjectInstance.group.id}:managedsubject:show">
                <g:link action="show" controller="managedSubject" id="${managedSubjectInstance.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view"/></g:link>
              </aaf:hasPermission>
            </td>
          </tr>
        </g:each>
        </tbody>
      </table>

  </g:if>
  <g:else>
    <p>No accounts of this type currently exist in this group.</p>
  </g:else>
</div>
