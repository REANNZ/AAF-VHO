
<html>
  <head>  
    <meta name="layout" content="internal" />
    <title><g:message encodeAs='HTML' code="views.aaf.base.workflow.approval.administrator.title" /></title>
  </head>
  <body>
    
    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.workflow.administrative.approval"/></li>
    </ul>

    <g:render template="/templates/flash" />

    <h2><g:message encodeAs='HTML' code="views.aaf.base.workflow.approval.administrative.heading" /></h2>

    <g:if test="${tasks}">
      <p><g:message encodeAs='HTML' code="views.aaf.base.workflow.approval.administrative.descriptive" /></p>
      <table class="table">
        <thead>
          <tr>
            <th><g:message encodeAs='HTML' code="label.name" /></th>
            <th><g:message encodeAs='HTML' code="label.created" /></th>
            <th><g:message encodeAs='HTML' code="label.processinstance" /></th>
            <th><g:message encodeAs='HTML' code="label.waitingon" /></th>
            <th><g:message encodeAs='HTML' code="label.action" /></th>
          </tr>
        </thead>
        <tbody>
          <g:each in="${tasks}" status="i" var="instance">
            <tr>
              <td>${fieldValue(bean: instance, field: "task.name")}</td>
              <td>${fieldValue(bean: instance, field: "dateCreated")}</td>
              <td>
                ${fieldValue(bean: instance, field: "processInstance.description")}
                <br><br>
                <ul class="clean">
                  <g:if test="${instance.processInstance.params.containsKey('identityProvider')}">
                    <li><n:button href="${createLink(controller:'IDPSSODescriptor', action:'show', id:instance.processInstance.params.identityProvider)}" label="${message(code: 'label.view')} ${message(code: 'label.identityprovider')}" class="view-button"/></li>
                  </g:if>
                  <g:if test="${instance.processInstance.params.containsKey('serviceProvider')}">
                    <li><n:button href="${createLink(controller:'SPSSODescriptor', action:'show', id:instance.processInstance.params.serviceProvider)}" label="${message(code: 'label.view')} ${message(code: 'label.serviceprovider')}" class="view-button"/></li>
                  </g:if>
                  <g:if test="${instance.processInstance.params.containsKey('organization') && instance.processInstance.params.organization.isNumber()}">
                    <li><n:button href="${createLink(controller:'organization', action:'show', id:instance.processInstance.params.organization)}" label="${message(code: 'label.view')} ${message(code: 'label.organization')}" class="view-button"/></li>
                  </g:if>
                </ul>
              </td>
              <td>
                <ul class="clean">
                <g:each in="${instance.potentialApprovers}" var="approver">
                  <li><g:link controller="subject" action="show" id="${approver.id}">${fieldValue(bean: approver, field: "email")}</g:link></li>
                </g:each>
                </ul>
              </td>
              <td>
                <g:form action="approve" id="${instance.id}" name="submitapproval${i}" style="margin-bottom:24px; padding:0;">
                  <g:submitButton name="submit" class="btn btn-success" value="${g.message(encodeAs:'HTML', code:'label.approve')}"/>
                </g:form>
                <g:each in="${instance.task.rejections}" var="rej">
                    <g:form action="reject" id="${instance.id}" name="submitrejection${i}" style="margin:1px; padding:0;">
                      <g:hiddenField name="rejection" value="${rej.key}" />
                      <g:submitButton name="submit" class="btn btn-danger" value="${rej.value.name}"/>
                    </g:form>
                </g:each>
              </td>
            </tr>
            <g:if test="${i+1 != tasks.size()}">
            <tr>
              <td colspan="4"><hr></td>
            </tr>
            </g:if>
          </g:each>
        </tbody>
      </table>
    </g:if>
    <g:else>
      <p class="alert alert-info"><g:message encodeAs='HTML' code="views.aaf.base.workflow.approval.administrator.nothing" /></p>
    </g:else>
      
  </body>
</html>
