package aaf.vhr.idp.http;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.nio.charset.StandardCharsets;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aaf.vhr.idp.VhrBasicAuthValidator;

public class VhrBasicAuthFilter implements Filter {

  private String realm;
  private VhrBasicAuthValidator vhrBasicAuthValidator;

  /** Name of request attribute to pass the username in */
  private String usernameRequestAttributeName = "VHRUsername";

  Logger log = LoggerFactory.getLogger("aaf.vhr.idp.http.VhrBasicAuthFilter");

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse res,
      FilterChain chain) throws IOException, ServletException {

    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) res;

    final String authorization = request.getHeader( "Authorization" );
    if(authorization != null && authorization.contains(" ")) {
      log.info("Attempting to establish session via Basic Auth");

      final String[] credentials = StringUtils.split( new String( Base64.decodeBase64( authorization.substring( authorization.indexOf(" ") ) ), StandardCharsets.UTF_8 ), ':' );

      if ( credentials.length == 2 ) {
        final String login = credentials[0];
        final String password = credentials[1];
        log.info ("Located basic authentication credentials for " + login + " validating password with VH.");
        final String remoteUser = vhrBasicAuthValidator.authenticate(login, password);
        log.debug("Username received: {}", remoteUser);

        if(remoteUser != null) {
          log.info ("Confirmed supplied credentials for " + credentials[0] + ", VH confirmed remoteUser value of " + remoteUser);
          request.setAttribute(usernameRequestAttributeName, remoteUser);
          chain.doFilter(request, response);

          return;
        }
      } else {
        log.info ("Invalid Authorization header detected when attempting to setup session");
      }
    }

    response.setHeader( "WWW-Authenticate", "Basic realm=\"" + realm + "\"" );
    response.sendError( HttpServletResponse.SC_UNAUTHORIZED );
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    realm = filterConfig.getInitParameter("realm");
    String apiServer = filterConfig.getInitParameter("apiServer");
    String apiEndpoint = filterConfig.getInitParameter("apiEndpoint");
    String apiToken = filterConfig.getInitParameter("apiToken");
    String apiSecret = filterConfig.getInitParameter("apiSecret");
    String requestingHost = filterConfig.getInitParameter("requestingHost");

    String uran = filterConfig.getInitParameter("usernameRequestAttributeName");
    if (uran != null) { usernameRequestAttributeName = uran; };

    vhrBasicAuthValidator = new VhrBasicAuthValidator(apiServer, apiEndpoint, apiToken, apiSecret, requestingHost);
  }

}
