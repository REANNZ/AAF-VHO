<html>
  <body class="plain">
      <h3><g:message encodeAs='HTML' code="label.subject"/>: ${fieldValue(bean: subject, field: "cn")}</h3>
      <hr>
      <div id="tab-overview" class="tab-pane active">
        <table class="table table-borderless fixed">
          <tbody>
            <tr>
              <th><g:message encodeAs='HTML' code="label.id"/></th>
              <td>${fieldValue(bean: subject, field: "id")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.principal"/></th>
              <td>${fieldValue(bean: subject, field: "principal")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.cn"/></th>
              <td>${fieldValue(bean: subject, field: "cn")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.email"/></th>
              <td>${fieldValue(bean: subject, field: "email")}</td>
            </tr>
            <tr>
              <th><g:message encodeAs='HTML' code="label.enabled"/></th>
              <td>${formatBoolean(boolean: subject.enabled)}</td>
            </tr>
          </tbody>
        </table>
      </div>
  </body>
</html>
