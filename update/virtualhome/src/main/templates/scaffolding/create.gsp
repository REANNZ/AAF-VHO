<html>
  <head>
    <meta name="layout" content="internal" />
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.${domainClass.propertyName.toLowerCase()}"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.${domainClass.propertyName.toLowerCase()}.create"/></li>
    </ul>

    <g:render template="/templates/flash" plugin="aafApplicationBase"/>
    <g:render template="/templates/errors_bean" model="['bean':${propertyName}]" plugin="aafApplicationBase"/>

    <h2><g:message encodeAs='HTML' code="views.${domainClass.packageName.toLowerCase()}.${domainClass.name.toLowerCase()}.create.heading" /></h2>
    
    <g:form action="save" class="form-validating form-horizontal">
      <g:render template="form"/>
      <div class="form-actions">
        <button type="submit" class="btn btn-success"/><g:message encodeAs='HTML' code="label.create" /></button>
        <g:link class="btn" action="list"><g:message encodeAs='HTML' code="label.cancel"/></g:link>
      </div>
    </g:form>
    
  </body>
</html>

