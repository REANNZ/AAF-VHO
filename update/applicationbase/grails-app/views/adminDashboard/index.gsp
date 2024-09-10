<html>
  <head>
    <meta name="layout" content="internal" />
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></li>
    </ul>

    <div class="row">
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${subjectCount}</strong>
          <hr>
          <h4>
            <g:link controller="subject" action="list"><g:message encodeAs='HTML' code="label.activesubjects"/></g:link>
          </h4>
        </div>
      </div>
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${disabledSubjectCount}</strong>
          <hr>
          <h4>
            <g:link controller="subject" action="list"><g:message encodeAs='HTML' code="label.disabledsubjects"/></g:link>
          </h4>
        </div>
      </div>
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${roleCount}</strong>
          <hr>
          <h4>
            <g:link controller="role" action="list"><g:message encodeAs='HTML' code="label.roles"/></g:link>
          </h4>
        </div>
      </div>
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${permCount}</strong>
          <hr>
          <h4><g:message encodeAs='HTML' code="label.permissions"/></h4>
        </div>
      </div>
    </div>

    <div class="row">
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${lastHourSessions}</strong>
          <hr>
          <h4><g:message encodeAs='HTML' code="label.sessionspasthour"/></h4>
        </div>
      </div>
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${lastDaySessions}</strong>
          <hr>
          <h4><g:message encodeAs='HTML' code="label.sessionspastday"/></h4>
        </div>
      </div>
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${lastWeekSessions}</strong>
          <hr>
          <h4><g:message encodeAs='HTML' code="label.sessionspastweek"/></h4>
        </div>
      </div>
      <div class="span3">
        <div class="well centered">
          <strong class="dashboard-wow">${lastMonthSessions}</strong>
          <hr>
          <h4><g:message encodeAs='HTML' code="label.sessionspast30days"/></h4>
        </div>
      </div>
    </div>

    <div class="row content-spacer">
      <div class="span12">
        <h3><g:message encodeAs='HTML' code="label.lastyearsessions"/></h3>
          <div id="sessionschart">
          </div>    
      </div>
    </div>

    <div class="row hidden-phone content-spacer">
      <div class="span12">
        <h3><g:message encodeAs='HTML' code="label.last25sessions"/></h3>
        <table class="table table-borderless table-sortable">
          <thead>
            <tr>
              <th><g:message encodeAs='HTML' code="label.cn" /></th>
              <th><g:message encodeAs='HTML' code="label.remotehost" /></th>
              <th><g:message encodeAs='HTML' code="label.useragent" /></th>
              <th><g:message encodeAs='HTML' code="label.datecreated" /></th>
              <th/>
            </tr>
          </thead>
          <tbody>
            <g:each in="${last25sessions}" status="i" var="session">
              <tr>
                <td>${fieldValue(bean: session, field: "subject.cn")}</td>
                <td>${fieldValue(bean: session, field: "remoteHost")}</td>
                <td>${fieldValue(bean: session, field: "userAgent")}</td>
                <td>${fieldValue(bean: session, field: "dateCreated")}</td>
                <td>
                  <g:link controller="subject" action="show" id="${session.subject.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view" /></g:link>
                </td>
              </tr>
            </g:each>
          </tbody>
        </table>
      </div>
    </div>

<r:script>
$(function() {
  aaf_base.administration_dashboard_sessions_report(${last12MonthSessions});      
});
</r:script>

  </body>
</html>
