<%@page import="edu.internet2.middleware.shibboleth.idp.session.Session" %>
<%@page import="edu.internet2.middleware.shibboleth.idp.session.ServiceInformation" %>
<%@page import="edu.internet2.middleware.shibboleth.idp.profile.saml2.SLOProfileHandler" %>
<%@page import="org.owasp.esapi.Encoder" %>
<%@page import="org.owasp.esapi.ESAPI" %>

<!-- for getting service name -->
<%@page import="edu.internet2.middleware.shibboleth.idp.util.HttpServletHelper" %>
<%@page import="edu.internet2.middleware.shibboleth.common.relyingparty.RelyingPartyConfigurationManager" %>
<%@page import="org.opensaml.saml2.metadata.EntityDescriptor" %>
<%@page import="org.opensaml.saml2.metadata.SPSSODescriptor" %>

<!-- Tuakiri extension: display the name of the service -->
<%!
String GetHostnameByURI(String uri)
{
  /* reusing uApprove's Controller.getResourceHost code where I've contributed */
    int i1 = uri.indexOf("//");
    int i2 = uri.indexOf("/", i1+2);

    // return just the sp.example.org component out of https://sp.example.org/shibboleth
    if ( i2 >= 0 )
       uri = uri.substring(i1 + 2, i2);
    else if ( i1 >= 0 )
       uri = uri.substring(i1 + 2);

    // return just the sp.example.org component out of urn:mace:federation.org:sp.example.org
    if (uri.indexOf(':')>=0) {
        uri = uri.substring(uri.lastIndexOf(':')+1);
    }

  return uri;
}
%>



<html>
  <head>
    <title>AAF Virtual Home Logout Page</title>
  </head>

  <body>
	<img src="<%= request.getContextPath()%>/images/dummylogo.png" alt="Replace or remove this logo"/>
    <h1>AAF Virtual Home Logout Page</h1>
    <p>You have successfully logged out of the AAF Virtual Home Identity Provider.</p>
    
    <p><strong>You have however NOT been logged out of any of the applications you
    have accessed during your session, with the possible exception of the Service Provider that may have
    initiated this logout operation.</strong></p>

    <p>The following is a list of Service Providers tracked by this session:</p>
    <%
    Session s = (Session) request.getAttribute(SLOProfileHandler.HTTP_LOGOUT_BINDING_ATTRIBUTE);
    if (s != null && !s.getServicesInformation().isEmpty()) {
    	Encoder esapi = ESAPI.encoder();
    %>
		<ul>
		<% for (ServiceInformation info : s.getServicesInformation().values()) { %>

<%

   String entityId = info.getEntityID();
   String serviceName = null;
   String hostname = entityId;

   try {
     RelyingPartyConfigurationManager rpConfigMngr = HttpServletHelper.getRelyingPartyConfigurationManager(request.getSession().getServletContext());
     EntityDescriptor metadata = HttpServletHelper.getRelyingPartyMetadata(entityId, rpConfigMngr);

     hostname = GetHostnameByURI(entityId);
     serviceName = ( (SPSSODescriptor)metadata.getRoleDescriptors(SPSSODescriptor.DEFAULT_ELEMENT_NAME).get(0) ).getAttributeConsumingServices().get(0).getNames().get(0).getName().getLocalString();

   } catch (Exception e) {
       /* Intentionally ignored:
        * Could not get detailed service name, will display basic name only */
   };
   String serviceDisplayName= (serviceName != null) ? serviceName + " (" + hostname + ")" : hostname;
%>

			<li><%= esapi.encodeForHTML(serviceDisplayName) %> </li>
		<% } %>
		</ul>	

	<% } else { %>
	
		<p>NONE FOUND</p>
	
	<% } %>
    <p>We recommend to close your browser to close all the sessions at the Service Providers.</p>

  </body>
</html>
