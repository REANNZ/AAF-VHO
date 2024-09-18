<html>
  <head>
    <meta name="layout" content="internal" />
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin.environment"/></li>
    </ul>

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.environment.heading"/></h2>
    <div class="row">
      <div class="span6">
        <h3><g:message encodeAs='HTML' code="views.aaf.base.admin.environment.appstatus"/></h3>
        <ul class="clean">
          <li>App version: <g:meta name="app.version"/></li>
          <li>Grails version: <g:meta name="app.grails.version"/></li>
          <li>JVM version: ${System.getProperty('java.version')}</li>
          <li>Reloading active: ${grails.util.Environment.reloadingAgentEnabled}</li>
          <li>Controllers: ${grailsApplication.controllerClasses.size()}</li>
          <li>Domains: ${grailsApplication.domainClasses.size()}</li>
          <li>Services: ${grailsApplication.serviceClasses.size()}</li>
          <li>Tag Libraries: ${grailsApplication.tagLibClasses.size()}</li>
        </ul>
      </div>
      <div class="span6">
        <h3><g:message encodeAs='HTML' code="views.aaf.base.admin.environment.plugins"/></h3>
        <ol>
          <g:each var="plugin" in="${applicationContext.getBean('pluginManager').allPlugins}">
            <li>${plugin.name} - ${plugin.version}</li>
          </g:each>
        </ol>
      </div>

  </body>
</html>
