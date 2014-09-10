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
<%@page import="java.util.regex.Pattern" %>
<%@page import="java.util.regex.Matcher" %>

<!-- Tuakiri extension: display the name of the service -->
<%!

    /** Regular Expression pattens used to extract the hostname out of an (entityID) URI in getHostnameByURI() */

    private static Pattern hostnamePatterns[] = { 

    /* entityId in the form https://sp.example.org/shibboleth
     * - https or http
     * - optional username and optional password (as non-capturing group)
     * - capture hostname
     * - optional port (non-capturing group)
     * - optionally followed by a slash, don't care about path component
     *
     */
          Pattern.compile("https?://(?:[^:@/]+(?::[^:@/]+)?@)?([^:/]+)(?::\\d+)?(?:/.*)?"),

    /* entityId in the form urn:mace:federation.org:sp.example.org
     * - urn:mace:
     * - sequnce of one or more : seperated name spaces
     * - capture hostname
     *
     */
          Pattern.compile("urn:mace:(?:[^:]+:)+([^:]+)")
    };

    private static String getHostnameByURI(String uri) {
      for (Pattern pattern : hostnamePatterns) {
         Matcher matcher = pattern.matcher(uri);
         if (matcher.matches() && matcher.groupCount()==1 ) {
              String hostname = matcher.group(1);
              return hostname;
         };
      };

      return uri;
    }
%>

<!-- Terminate session cookie created by LoginController for VhrFilter -->
<%!
    /* originally defined in aaf.vhr.LoginService */
    static final String SSO_COOKIE_NAME = "_vh_l1";

    /* defined in application_config as aaf.vhr.login.path, defaults to "/" */
    static final String SSO_COOKIE_PATH = "/";
%>
<%

    Cookie c = new Cookie(SSO_COOKIE_NAME, null);
    c.setPath(SSO_COOKIE_PATH);
    c.setMaxAge(0);
    c.setSecure(true);  //But beware of older versions of
	    // application_config.groovy.orig setting
	    // aaf.vhr.login.ssl_only_cookie to false
    response.addCookie(c);

%>


<html>
  <head>
    <title>Tuakiri Virtual Home Logout Page</title>
  </head>

  <body>
    <h1>Tuakiri Virtual Home Logout Page</h1>
    <p>You have successfully logged out of the Tuakiri Virtual Home Identity Provider.</p>
    
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

     hostname = getHostnameByURI(entityId);
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
