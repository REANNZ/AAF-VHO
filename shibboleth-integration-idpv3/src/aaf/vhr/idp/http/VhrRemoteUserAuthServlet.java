/*
 * Licensed to the University Corporation for Advanced Internet Development,
 * Inc. (UCAID) under one or more contributor license agreements.  See the
 * NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The UCAID licenses this file to You under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package aaf.vhr.idp.http;

import java.io.IOException;
import java.security.Principal;
import java.time.Instant;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.security.auth.Subject;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import net.shibboleth.idp.authn.context.AuthenticationContext;
import net.shibboleth.idp.authn.context.RequestedPrincipalContext;
import net.shibboleth.idp.authn.principal.UsernamePrincipal;
import net.shibboleth.idp.authn.AuthenticationFlowDescriptor;
import net.shibboleth.idp.authn.ExternalAuthentication;
import net.shibboleth.idp.authn.ExternalAuthenticationException;
import net.shibboleth.idp.consent.context.ConsentManagementContext;
import net.shibboleth.idp.ui.context.RelyingPartyUIContext;
import net.shibboleth.shared.annotation.constraint.NotEmpty;

import org.opensaml.profile.context.ProfileRequestContext;
import org.apache.commons.codec.EncoderException;
import org.apache.commons.codec.net.URLCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import aaf.vhr.idp.VhrSessionValidator;

/**
 * Authenticate a user against the VHR.
 */
public class VhrRemoteUserAuthServlet extends HttpServlet {

    /** Serial UID. */
    private static final long serialVersionUID = -4936410983928392293L;

    /** Class logger. */
    @Nonnull private final Logger log = LoggerFactory.getLogger(VhrRemoteUserAuthServlet.class);

    // VHR-specific attributes
    final String SSO_COOKIE_NAME = "_vh_l1";

    final String IS_FORCE_AUTHN_ATTR_NAME = "aaf.vhr.idp.http.VhrRemoteUserAuthServlet.isForceAuthn.";
    final String AUTHN_INIT_INSTANT_ATTR_NAME = "aaf.vhr.idp.http.VhrRemoteUserAuthServlet.authnInitInstant.";
    final String REDIRECT_REQ_PARAM_NAME = "vhr.key";

    private String vhrLoginEndpoint;
    private VhrSessionValidator vhrSessionValidator;

    /** Name of the request parameter that would indicate the user wants to revoke consent */
    private String consentRevocationParamName = "_shib_idp_revokeConsent";

    /** Principal name to add to Subject only when MFA is used. */
    private String mfaPrincipalName;

// Checkstyle: CyclomaticComplexity OFF
    /** {@inheritDoc} */
    @Override
    public void init(final ServletConfig config) throws ServletException {
        super.init(config);

        // VHR-specific initalization
        vhrLoginEndpoint = config.getInitParameter("loginEndpoint");
        String apiServer = config.getInitParameter("apiServer");
        String apiEndpoint = config.getInitParameter("apiEndpoint");
        String apiToken = config.getInitParameter("apiToken");
        String apiSecret = config.getInitParameter("apiSecret");
        String requestingHost = config.getInitParameter("requestingHost");

        // Consent revocation parameter name: override default if set
        String crpn = config.getInitParameter("consentRevocationParamName");
        if (crpn != null) { consentRevocationParamName = crpn; };

        mfaPrincipalName = config.getInitParameter("mfaPrincipalName");

        vhrSessionValidator = new VhrSessionValidator(apiServer, apiEndpoint, apiToken, apiSecret, requestingHost);

    }

// Checkstyle: MethodLength OFF
    /** {@inheritDoc} */
    @Override
    protected void service(final HttpServletRequest httpRequest, final HttpServletResponse httpResponse)
            throws ServletException, IOException {

        try {
            // key to ExternalAuthentication session
            String key = null;
            boolean isVhrReturn = false;
            boolean isForceAuthn = false;
            Instant authnStart = null; // when this authentication started at the IdP
            // arrays to use as return parameter when calling VhrSessionValidator
            Instant authnInstantArr[] = new Instant[1];
            boolean mfaArr[] = new boolean[1];

            if (httpRequest.getParameter(REDIRECT_REQ_PARAM_NAME) != null) {
                // we have come back from the VHR
                isVhrReturn = true;
                key = httpRequest.getParameter(REDIRECT_REQ_PARAM_NAME);
                HttpSession hs = httpRequest.getSession();

                if (hs != null && hs.getAttribute(AUTHN_INIT_INSTANT_ATTR_NAME + key) != null ) {
                   authnStart = (Instant)hs.getAttribute(AUTHN_INIT_INSTANT_ATTR_NAME + key);
                   // remove the attribute from the session so that we do not attempt to reuse it...
                   hs.removeAttribute(AUTHN_INIT_INSTANT_ATTR_NAME);
                };

                if (hs != null && hs.getAttribute(IS_FORCE_AUTHN_ATTR_NAME + key) != null ) {
                   isForceAuthn = ((Boolean)hs.getAttribute(IS_FORCE_AUTHN_ATTR_NAME + key)).booleanValue();
                   // remove the attribute from the session so that we do not attempt to reuse it...
                   hs.removeAttribute(IS_FORCE_AUTHN_ATTR_NAME);
                };

            } else {
                // starting a new SSO request
                key = ExternalAuthentication.startExternalAuthentication(httpRequest);

                // check if forceAuthn is set
                Object forceAuthnAttr = httpRequest.getAttribute(ExternalAuthentication.FORCE_AUTHN_PARAM);
                if ( forceAuthnAttr != null && forceAuthnAttr instanceof java.lang.Boolean) {
                    log.debug("Loading foceAuthn value");
                    isForceAuthn = ((Boolean)forceAuthnAttr).booleanValue();
                }

                // check if we can see when authentication was initiated
                final AuthenticationContext authCtx =
                        ExternalAuthentication.getProfileRequestContext(key, httpRequest).
                            getSubcontext(AuthenticationContext.class,false);
                if (authCtx != null) {
                    log.debug("Authentication initiation is {}", authCtx.getInitiationInstant());
                    authnStart = authCtx.getInitiationInstant();
                    log.debug("AuthnStart is {}", authnStart);
                };

            };
            log.debug("forceAuthn is {}, authnStart is {}", isForceAuthn, authnStart);

            if (key == null) {
                log.error("No ExternalAuthentication sesssion key found");
                throw new ServletException("No ExternalAuthentication sesssion key found");
            };
            // we now have a key - either:
            // * we started new authentication
            // * or we have returned from VHR and loaded the key from the HttpSession

            // Determine whether MFA is requested
            final ProfileRequestContext prc = ExternalAuthentication.getProfileRequestContext(key, httpRequest);
            // get RequestedPrincipalContext to get list of requested principals
            final RequestedPrincipalContext rqPCtx = prc.getSubcontext(AuthenticationContext.class,true).
                    getSubcontext(RequestedPrincipalContext.class, false);
            boolean mfaRequested = false;

            if (rqPCtx != null && mfaPrincipalName != null) {
                for (final Principal p: rqPCtx.getRequestedPrincipals()) {
                    if (p.getName().equals(mfaPrincipalName)) {
                        mfaRequested = true;
                        log.debug("MFA Principal {} requested, signalling to application.", p.getName());
                    }
                };
            };

            String username = null;

            // We may have a cookie - either as part of return or from previous session
            // Attempt to locate VHR SessionID
            String vhrSessionID = null;
            Cookie[] cookies = httpRequest.getCookies();
            for(Cookie cookie : cookies) {
                if(cookie.getName().equals(SSO_COOKIE_NAME)) {
                    vhrSessionID = cookie.getValue();
                    break;
                }
            }

            if (vhrSessionID != null) {
                log.info("Found vhrSessionID from {}. Establishing validity.", httpRequest.getRemoteHost());
                // Force a new login attempt if caching a non-MFA login but MFA is requested
                // Accept whatever is returned on returning from VHO (and let downstream deal with lack of MFA)
                final boolean mfaInsist = mfaRequested && !isVhrReturn;
                username = vhrSessionValidator.validateSession(vhrSessionID, ( isForceAuthn ? authnStart : null), mfaInsist, authnInstantArr, mfaArr);
            };

            // If we do not have a username yet (no Vhr session cookie or did not validate),
            // we redirect to VHR - but only if we are not returning from the VHR
            // Reason: (i) we do not want to loop and (ii) we do not have the full context otherwise initialized by
            // ExternalAuthentication.startExternalAuthentication()
            if ( username == null && !isVhrReturn ) {

                URLCodec codec = new URLCodec();
                String relyingParty = (String)httpRequest.getAttribute("relyingParty");
                String serviceName = "";

                log.info("No vhrSessionID found from {}. Directing to VHR authentication process.", httpRequest.getRemoteHost());
                log.debug("Relying party which initiated the SSO request was: {}", relyingParty);

                // try getting a RelyingPartyUIContext
                // we should pass on the request for consent revocation
                final RelyingPartyUIContext rpuiCtx = prc.getSubcontext(AuthenticationContext.class,true).
                        getSubcontext(RelyingPartyUIContext.class, false);
                if (rpuiCtx != null) {
                    serviceName = rpuiCtx.getServiceName();
                    log.debug("RelyingPartyUIContext received, ServiceName is {}", serviceName);
                };

                // save session *key*
                HttpSession hs = httpRequest.getSession(true);
                hs.setAttribute(IS_FORCE_AUTHN_ATTR_NAME + key, Boolean.valueOf(isForceAuthn));
                hs.setAttribute(AUTHN_INIT_INSTANT_ATTR_NAME + key, authnStart);

                try {
                    httpResponse.sendRedirect(String.format(vhrLoginEndpoint,
                            codec.encode(httpRequest.getRequestURL().toString()+"?"+REDIRECT_REQ_PARAM_NAME+"="+codec.encode(key)),
                            codec.encode(relyingParty),
                            codec.encode(serviceName),
                            codec.encode(Boolean.toString(mfaRequested))));
                } catch (EncoderException e) {
                    log.error ("Could not encode VHR redirect params");
                    throw new IOException(e);
                }
                return; // we issued a redirect - return now
            };

            if (username == null) {
                log.warn("VirtualHome authentication failed: no username received");
                httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_ERROR_KEY, "VirtualHome authentication failed: no username received");
                ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);
                return;
            }

            // check if consent revocation was requested
            String consentRevocationParam = httpRequest.getParameter(consentRevocationParamName);
            if (consentRevocationParam != null) {
                // we should pass on the request for consent revocation
                final ConsentManagementContext consentCtx = prc.getSubcontext(ConsentManagementContext.class, true);
                log.debug("Consent revocation request received, setting revokeConsent in consentCtx");
                consentCtx.setRevokeConsent(consentRevocationParam.equalsIgnoreCase("true"));
            };

            // Set authnInstant to timestamp returned by VHR
            if (authnInstantArr[0] != null) {
                log.debug("Response from VHR includes authenticationInstant time {}, passing this back to IdP", authnInstantArr[0]);
                httpRequest.setAttribute(ExternalAuthentication.AUTHENTICATION_INSTANT_KEY, authnInstantArr[0]);
            };

            final Subject subject = new Subject();
            subject.getPrincipals().add(new UsernamePrincipal(username));

            // If mfaPrincipalName is configured, add the correct set of principals to Subject.
            // The principal name matching MFA will only be added if MFA was used.
            // Other principal names supported by this flow are assumed to be SFA and will be added regardless of MFA.
            if (mfaPrincipalName != null) {
                final AuthenticationFlowDescriptor authnFlow = getAuthenticationFlowDescriptor(key, httpRequest);
                boolean mfaPrincipalFound = false;
                for (final Principal p :authnFlow.getSupportedPrincipals()) {
                    if (p.getName().equals(mfaPrincipalName)) {
                        mfaPrincipalFound = true;
                        if (mfaArr[0]) {
                            log.debug("MFA was used, passing MFA principal {} back to IdP", p.getName());
                            subject.getPrincipals().add(p);
                        } else {
                            log.debug("MFA was not used, skipping MFA principal {}", p.getName());
                        }
                    } else {
                        log.debug("Passing non-MFA principal {} back to IdP", p.getName());
                        subject.getPrincipals().add(p);
                    };
                };
                if (mfaArr[0] && !mfaPrincipalFound) {
                    log.warn("Response from VHR indicates MFA status was used, but principal {} is not configured as supported by this profile", mfaPrincipalName);
                }
            };

            // return subject with at least UsernamePrincipal and optional set of authentication context principals
            httpRequest.setAttribute(ExternalAuthentication.SUBJECT_KEY, subject);

            ExternalAuthentication.finishExternalAuthentication(key, httpRequest, httpResponse);

        } catch (final ExternalAuthenticationException e) {
            throw new ServletException("Error processing external authentication request", e);
        }
    }
// Checkstyle: CyclomaticComplexity|MethodLength ON

    /**
     * Get the executing {@link AuthenticationFlowDescriptor}.
     *
     * Reused from RemoteUserAuthServlet.
     *
     * @param key external authentication key
     * @param httpRequest servlet request
     *
     * @return active descriptor, or null
     * @throws ExternalAuthenticationException  if unable to access the profile context
     */
    @Nullable public AuthenticationFlowDescriptor getAuthenticationFlowDescriptor(@Nonnull @NotEmpty final String key,
            @Nonnull final HttpServletRequest httpRequest) throws ExternalAuthenticationException {

        final ProfileRequestContext prc =
                ExternalAuthentication.getProfileRequestContext(key, httpRequest);
        final AuthenticationContext authnCtx = prc.getSubcontext(AuthenticationContext.class);
        return (authnCtx != null) ? authnCtx.getAttemptedFlow() : null;
    }

}
