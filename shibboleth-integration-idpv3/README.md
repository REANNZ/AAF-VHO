
# Shibboleth IdP 3.x and 4.x integration code #

This code was developed by REANNZ (Research and Education Advanced Network New Zealand) for Tuakiri, the New Zealand Access Federation, and contributed back to the AAF virtualhome project.

This code allows running the virtualhome application with a Shibboleth 3.x or 4.x IdP.

This module initially supported IdP 3.x (up to VirtualHome version 1.6.5, [1.6.5-tuakiri5](https://github.com/REANNZ/AAF-VHO/releases/tag/1.6.5-tuakiri5).  Subsequent versions have been adjusted for IdP 4.x - the archive of the repo can be used to retrieve a 3.x compatible version.

Tuakiri is running this code in production, with a customized version of the virtualhome application.

To use the full functionality of this code, one would need to use the version of the virtualhome application extended by REANNZ for Tuakiri.  Specifically:
* support for `forceAuthn="true"` requires the virtualhome application to return the timestamp of the authentication on the API exposed to this module.  This was introduced as an extension in the Tuakiri version.
* the module also tries to pass the service name to the login page - but this get only accepted by the Tuakiri version.
* support for signalling whether MFA was used through the set of principals returned.  This mode gets activated by setting the `mfaPrincipalName` initialisation parameter for the `VhrRemoteUserAuthServlet`.
  *  In this mode, the servlet iterates over the principals (authentication contexts) configured as supported by the authentication flow, and adds them to the returned Subject according to the following rules:
    * the principal matching the mfaPrincipalName parameter is only added when MFA is used by the user account.
    * all other principals are assumed to represent Single Authentication Factor (SFA) flows and are added regardless whether MFA was used in the session.
  * Note that this mode requires version of the VHO app extended to include the MFA status in the authentication response.
  * Note also that it requires the IdP to have the RemoteUser authentication flow configured with correct set of supportedPrincipals, extended to include the principal referred to by `mfaPrincipalName` (recommended to use `https://refeds.org/profile/mfa`).
  * It also requires the `RemoteUser` authentication flow configured not to automatically add all support principals (which would conflict with the approach of adding the MFA principal only when MFA is actually used).  This is achieved by setting the `addDefaultPrincipals` bean (or property) to False.

This module MAY work with the AAF version of virtualhome (without the two features above), but AAF provides absolutely no support for this module.

## Installation and configuration ##

* build the module with

    gradle build

* use the configuration snippets in the idpv3 directory here:
  * add the contents of `addto-web.xml` to `edit-webapp/WEB-INF/web.xml`
  * add `VHRUsername` (or a different value as configured in `web.xml` above) to the `shibboleth.authn.RemoteUser.checkAttributes` bean in `conf/authn/remoteuser-authn-config.xml` as per `change-in-authn-remoteuser-internal-authn-config.xml`
  * optionally, to support `forceAuthn`, change the `authn/RemoteUser` bean in `conf/authn/general-authn.xml` to declare support for forceAuthn as per the sample in `change-in-authn-general-authn.xml`

## Authors ##

This module was developed by Vlad Mencl <vladimir.mencl@reannz.co.nz>, based on the AAF IdPV2 module.
