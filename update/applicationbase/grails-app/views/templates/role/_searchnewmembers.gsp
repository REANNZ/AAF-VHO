<legend><g:message encodeAs='HTML' code="label.addmembers"/></legend>
<table class="table table-borderless table-sortable">
  <thead>
    <tr>
      <th><g:message encodeAs='HTML' code="label.id" /></th>
      <th><g:message encodeAs='HTML' code="label.name" /></th>
      <th><g:message encodeAs='HTML' code="label.principal" /></th>
      <th/>
    </tr>
  </thead>
  <tbody>
    <g:each in="${subjects}" var="subject">
      <tr>
        <td><g:fieldValue bean="${subject}" field="id"/></td>
        <td><g:fieldValue bean="${subject}" field="cn"/></td>
        <td><g:fieldValue bean="${subject}" field="principal"/></td>
        <td class="pull-right">
          <g:form method="post">
            <g:hiddenField name="id" value="${role?.id}" />
            <g:hiddenField name="subjectID" value="${subject?.id}" />
            <a href="#" class="btn btn-small ajax-modal" data-load="${createLink(controller:'subject', action:'showpublic', id:subject.id, absolute:true)}" ><g:message encodeAs='HTML' code="label.quickview" /></a>
            <g:link controller="subject" action="show" id="${subject.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view" /></g:link>
            <g:actionSubmit action="addmember" class="btn btn-small" value="${message(code: 'label.add', default: 'Add')}" />
          </g:form>
        </td>
    </g:each>
  </tbody>
</table>
