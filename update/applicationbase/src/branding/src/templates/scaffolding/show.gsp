<% import grails.persistence.Event %>
<html>
  <head>
    <meta name="layout" content="internal" />
  </head>

  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li><g:link action="list"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.${domainClass.propertyName.toLowerCase()}"/></g:link> <span class="divider">/</span></li>
      <li><g:fieldValue bean="\${${propertyName}}" field="id"/></li>
    </ul>

    <g:render template="/templates/flash" plugin="aafApplicationBase"/>
    <g:render template="/templates/errors_bean" model="['bean':${propertyName}]" plugin="aafApplicationBase"/>

    <h2><g:message encodeAs='HTML' code="views.${domainClass.packageName.toLowerCase()}.${domainClass.name.toLowerCase()}.show.heading" args="[]"/></h2>

    <ul class="nav nav-tabs">
      <li class="active"><a href="#tab-overview" data-toggle="tab"><g:message encodeAs='HTML' code="label.overview" /></a></li>

      <aaf:hasAnyPermission in='["app:manage:${domainClass.name.toLowerCase()}:\${${propertyName}.id}:edit","app:manage:${domainClass.name.toLowerCase()}:\${${propertyName}.id}:delete"]'>
      <li class="dropdown pull-right">
        <a class="dropdown-toggle" data-toggle="dropdown" href="#">
          <g:message encodeAs='HTML' code="label.actions" />
          <b class="caret"></b>
        </a>
        <ul class="dropdown-menu">
          <aaf:hasPermission target="app:manage:${domainClass.name.toLowerCase()}:\${${propertyName}.id}:edit">
            <li>
              <g:link action="edit" id="\${${propertyName}.id}"><g:message encodeAs='HTML' code="label.edit"/></g:link>
            </li>
          </aaf:hasPermission>

          <aaf:hasPermission target="app:manage:${domainClass.name.toLowerCase()}:\${${propertyName}.id}:delete">
            <li>
              <a href="#" class="delete-ensure" data-confirm="\${message(code:'views.${domainClass.packageName.toLowerCase()}.${domainClass.name.toLowerCase()}.confirm.remove')}"><g:message encodeAs='HTML' code="label.delete"/></a>
              <g:form action="delete" method="delete">
                <g:hiddenField name="id" value="\${${propertyName}.id}" />
              </g:form>
            </li>
          </aaf:hasPermission>
        </ul>
      </li>
      </aaf:hasAnyPermission>
    </ul>

    <div class="tab-content">
      <div id="tab-overview" class="tab-pane active">
        <table class="table table-borderless">
          <tbody>
            <%  excludedProps = Event.allEvents.toList() << 'id' << 'version'
            allowedNames = domainClass.persistentProperties*.name << 'dateCreated' << 'lastUpdated'
            props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) }
            Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
            props.each { p -> %>
            <tr>
              <th class="span4"><span id="${p.name.toLowerCase()}-label"><strong><g:message encodeAs='HTML' code="label.${p.name.toLowerCase()}" /></strong></span></th><%  if (p.isEnum()) { %>
              <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:fieldValue bean="\${${propertyName}}" field="${p.name}"/></span>
              <%  } else if (p.oneToMany || p.manyToMany) { %>
              <g:if test="\${${propertyName}.${p.name}}">
                <g:each in="\${${propertyName}.${p.name}}" var="${p.name[0]}">
                  <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${p.name[0]}.id}">\${${p.name[0]}?.encodeAsHTML()}</g:link></span>
                </g:each>
              </g:if>
              <g:else>
                <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:message encodeAs='HTML' code="label.none" /></span>
              </g:else>
            <%  } else if (p.manyToOne || p.oneToOne) { %>
              <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:link controller="${p.referencedDomainClass?.propertyName}" action="show" id="\${${propertyName}?.${p.name}?.id}">\${${propertyName}?.${p.name}?.encodeAsHTML()}</g:link></span>
            <%  } else if (p.type == Boolean || p.type == boolean) { %>
              <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:formatBoolean boolean="\${${propertyName}?.${p.name}}" /></span>
            <%  } else if (p.type == Date || p.type == java.sql.Date || p.type == java.sql.Time || p.type == Calendar) { %>
              <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:formatDate date="\${${propertyName}?.${p.name}}" /></span>
            <%  } else if(!p.type.isArray()) { %>
              <td><span aria-labelledby="${p.name.toLowerCase()}-label"><g:fieldValue bean="\${${propertyName}}" field="${p.name}"/></span>
            <%  } %></tr>
          <%  } %>
          </tbody>
        </table>
      </div>
    </div>

  </body>
</html>
