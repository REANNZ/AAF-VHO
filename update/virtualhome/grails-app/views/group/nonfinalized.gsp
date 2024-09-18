<html>
  <head>
    <meta name="layout" content="internal" />
  </head>

  <body>
    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link action="show" controller="organization" id="${groupInstance.organization.id}"><g:fieldValue bean="${groupInstance.organization}" field="displayName"/></g:link> <span class="divider">/</span></li>
      <li><g:link action="show" controller="group" id="${groupInstance.id}"><g:fieldValue bean="${groupInstance}" field="name"></g:fieldValue></g:link> <span class="divider">/</span></li>
      <li><g:message encodeAs='HTML' code="views.aaf.vhr.group.nonfinalized.breadcrumb" /></li>
    </ul>

    <g:render template="/templates/flash" plugin="aafApplicationBase" />
    <g:render template="/templates/errors_bean" model="['bean':groupInstance]" plugin="aafApplicationBase" />

    <h2><g:message encodeAs='HTML' code="views.aaf.vhr.group.nonfinalized.heading" args="${[groupInstance.name]}"/></h2>
    <p><g:message encodeAs='HTML' code="views.aaf.vhr.group.nonfinalized.currentdetails" args="${[groupInstance.name]}"/></p>

    <table>
      <tbody>
        <tr>
          <td class="span4"><g:message code="label.totalaccounts"/></td>
          <td>${groupInstance.subjects.size()}</td>
        </tr>
        <tr>
          <td class="span4"><g:message code="label.totalfinalizedaccounts"/></td>
          <td>${groupInstance.subjects.size() - groupInstance.nonFinalizedAccounts().size()}</td>
        </tr>
        <tr>
          <td class="span4"><g:message code="label.totalnonfinalizedaccounts"/></td>
          <td>${groupInstance.nonFinalizedAccounts().size()}</td>
        </tr>
      </tbody>
    </table>

    <h3><g:message encodeAs='HTML' code="views.aaf.vhr.group.nonfinalized.subjects.heading"></g:message></h3>
    <g:message code="views.aaf.vhr.group.nonfinalized.accountdetail" />

    <div id="nonfinalizedaccounts">
      <table class="exportable-table span12">
        <thead>
          <tr>
            <th><g:message code="label.created"></g:message></th>
            <th><g:message code="label.name"></g:message></th>
            <th><g:message code="label.email"></g:message></th>
            <th><g:message code="label.finalizationurl"></g:message></th>
            <th class="exportable-exclude"/>
          </tr>
        </thead>
        <tbody>
        <g:each in="${groupInstance.nonFinalizedAccounts().sort{it.dateCreated}}" var="subject">
          <tr>
            <td><g:formatDate date="${subject?.dateCreated}" /></td>
            <td><g:fieldValue bean="${subject}" field="cn"/></td>
            <td><g:fieldValue bean="${subject}" field="email"/></td>
            <td><g:link controller="finalization" action="index" absolute="true" params="[inviteCode: subject.invitations.first().inviteCode]"><g:createLink controller="finalization" action="index" absolute="true" params="[inviteCode: subject.invitations.first().inviteCode]" /></g:link></td>
            <td class="exportable-exclude"><g:link controller="managedSubject" action="show" id="${subject.id}" class="btn btn-mini"><g:message code="label.view"/></g:link></td>
          </tr>
        </g:each>
      </table>

      <p class="pull-right"><br><a href="#" class="export btn btn-link btn-mini">Export to Excel (CSV) <i class="icon-share"></i> </a></p>
    </div>
  </body>
</html>