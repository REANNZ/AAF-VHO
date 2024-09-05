## Shibboleth SP Setup

Setup and configure a Shibboleth service provider per your federations instructions.

For the AAF this is http://wiki.aaf.edu.au/tech-info/sp-install-guide

For other federations you may like to start at https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPLinuxRPMInstall

Be sure to configure the SP for a Java application environment per https://wiki.shibboleth.net/confluence/display/SHIB2/NativeSPJavaInstall noting especially the `attributePrefix="AJP_"` requirement.

Required Shibboleth mappings are covered in the provided configuration/apache/vho.conf (essentially '/').
