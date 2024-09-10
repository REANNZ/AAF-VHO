
<html>
  <head>
    <r:require modules="codemirror"/>
    <meta name="layout" content="internal" />
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="adminDashboard"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="emailTemplate" action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin.emailtemplate"/></g:link> <span class="divider">/</span></li>
      <li><g:link controller="emailTemplate" action="show" id="${emailtemplate.id}">${fieldValue(bean: emailtemplate, field: "name")}</g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.admin.emailtemplate.edit"/></li>
    </ul>

    <g:render template="/templates/flash" />
    <g:render template="/templates/errors_bean" model="['bean':emailtemplate]"/>

    <h2><g:message encodeAs='HTML' code="views.aaf.base.admin.emailtemplate.edit.heading" args="[emailtemplate.name]"/></h2>
    
      <g:form action="update" id="${emailtemplate.id}" class="form-validating">
        <div class="row">
          <div class="span4">   
            <table>
              <tbody>
                <tr>
                  <td><label for="name"><g:message encodeAs='HTML' code="label.name" /></label></td>
                  <td><g:textField name="name" value="${emailtemplate.name}" class="required"/></td>
                </tr>
              </tbody>
            </table>
          </div>
          <div class="span8">
            <g:textArea name="content" value="${emailtemplate.content}" rows="5" cols="40" class="required"/>
          </div>
        </div>
        <div class="form-actions">
          <button type="submit" class="btn btn-success"/><g:message encodeAs='HTML' code="label.update" /></button>
          <g:link class="btn" controller="emailTemplate" action="show" id="${emailtemplate.id}"><g:message encodeAs='HTML' code="label.cancel"/></g:link>
        </div>
      </g:form>
    
      <r:script>
        var editor = CodeMirror.fromTextArea('content',  {
          height: "600px",
          path: "",
          stylesheet: "${r.resource(dir:'/js/codemirror', file:'groovycolors.css', plugin:'aafApplicationBase') }",
          basefiles: ["${r.resource(dir:'/js/codemirror', file:'codemirror.groovy.inframe.min.js', plugin:'aafApplicationBase') }"],
          parserfile: [],
          autoMatchParens: true,
          disableSpellcheck: true,
          lineNumbers: true,
          tabMode: 'shift'
        });
      </r:script>
  </body>
</html>
