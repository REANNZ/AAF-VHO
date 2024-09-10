<% import grails.persistence.Event %>
<html>
  <head>  
    <meta name="layout" content="internal" />
  </head>
  <body>

    <ul class="breadcrumb">
      <li><g:link controller="dashboard"><g:message encodeAs='HTML' code="branding.application.name"/></g:link> <span class="divider">/</span></li>
      <li class="active"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.${domainClass.propertyName.toLowerCase()}"/></li>
      
      <aaf:hasPermission target="app:manage:${domainClass.propertyName.toLowerCase()}:create">
        <li class="pull-right"><strong><g:link action="create"><g:message encodeAs='HTML' code="branding.nav.breadcrumb.${domainClass.propertyName.toLowerCase()}.create"/></g:link></strong></li>
      </aaf:hasPermission>
    </ul>

    <g:render template="/templates/flash" plugin="aafApplicationBase"/>

    <h2><g:message encodeAs='HTML' code="views.${domainClass.packageName.toLowerCase()}.${domainClass.name.toLowerCase()}.list.heading" /></h2>
    
      <table class="table table-borderless table-sortable">
        <thead>
          <tr><%  excludedProps = Event.allEvents.toList() << 'id' << 'version' << 'dateCreated' << 'lastUpdated'
            allowedNames = domainClass.persistentProperties*.name
            props = domainClass.properties.findAll { allowedNames.contains(it.name) && !excludedProps.contains(it.name) && it.type != null && !Collection.isAssignableFrom(it.type) }
            Collections.sort(props, comparator.constructors[0].newInstance([domainClass] as Object[]))
            props.eachWithIndex { p, i ->
              if (i < 6) { %>
              <th><g:message encodeAs='HTML' code="label.${p.name.toLowerCase()}" /></th> <%    }   } %>
              <th/>
          </tr>
        </thead>
        <tbody>
        <g:each in="\${${propertyName}List}" status="i" var="${propertyName}">
          <tr class="\${(i % 2) == 0 ? 'even' : 'odd'}"><%  props.eachWithIndex { p, i ->
              if (i < 6) {
                if (p.type == Boolean || p.type == boolean) { %>
            <td><g:formatBoolean boolean="\${${propertyName}.${p.name}}" /></td><%    } else if (p.type == Date || p.type == java.sql.Date || p.type == java.sql.Time || p.type == Calendar) { %>
            <td><g:formatDate date="\${${propertyName}.${p.name}}" /></td><%    } else { %>
            <td>\${fieldValue(bean: ${propertyName}, field: "${p.name}")}</td><%    }   }   } %>
            <td><g:link action="show" id="\${${propertyName}.id}" class="btn btn-small"><g:message encodeAs='HTML' code="label.view"/></g:link></td>
          </tr>
        </g:each>
        </tbody>
      </table>
  </body>
</html>
