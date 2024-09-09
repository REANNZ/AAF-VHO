<html>
  <head>
    <meta name="layout" content="public" />
  </head>
  
  <body>

      <div class="row">
        <div class="span12">
          <div class="hero-unit">
              <h2>Development Login - <g:message encodeAs='HTML' code='branding.application.name' /></h2>
              <p>Please choose either federated or local account login for development purposes. For each local account in development mode different access levels are provided.</p>
              <p>This functionality <strong>is not available in production</strong>  (.WAR) deployments.</p>
          </div>
        </div>
      </div>

      <div class="row">
        <div class="span6">
          <h2>Federated Login</h2>
          <p>Login using a Shibboleth SP configured against your local development environemt.</p>
          <a href="${spsession_url}" class="btn btn-info btn-large">Federated Login via Shibboleth SP</a>
        </div>
        <div class="span6">
          <h2>Local Accounts</h2>
          <p>Login using a temporary account for easy development. Modify the posted details to modify the user.</p>
          <div class="">
            <h3>Fred Bloggs</h3>
            <p>Principal: https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-1</p>
              <g:form controller="federatedSessions" action="federatedlogin" method="post">
              <g:hiddenField name="attributes.persistent-id" value="https://idp.one.edu.au/idp/shibboleth!https://manager.aaf.edu.au/shibboleth!8698dde3-d9cd-4c44-928c-2b6836bcc4bb" />
              <g:hiddenField name="attributes.Shib-Session-ID" value="fake-sessionid-webform" />
              <g:hiddenField name="attributes.Shib-Identity-Provider" value="https://idp.one.edu.au/idp/shibboleth" />
              <g:hiddenField name="attributes.auEduPersonSharedToken" value="TUWr3JO7Zdf1grqyRHiuBXdf-m1" />
              <g:hiddenField name="attributes.cn" value="Fred Bloggs" />
              <g:hiddenField name="attributes.mail" value="fredbloggs@one.edu.au" />

              <g:submitButton name="Login as Fred Bloggs" class="btn btn-success btn-large"/>
            </g:form>
          </div>
          <div class="">
            <h3>Joe Schmoe</h3>
            <p>Principal: https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-2</p>
              <g:form controller="federatedSessions" action="federatedlogin" method="post">
              <g:hiddenField name="attributes.persistent-id" value="https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-2" />
              <g:hiddenField name="attributes.Shib-Session-ID" value="fake-sessionid-webform" />
              <g:hiddenField name="attributes.Shib-Identity-Provider" value="https://idp.one.edu.au/idp/shibboleth" />
              <g:hiddenField name="attributes.auEduPersonSharedToken" value="TUWr3JO7Zdf1grqyRHiuBXdf-m2" />
              <g:hiddenField name="attributes.cn" value="Joe Schmoe" />
              <g:hiddenField name="attributes.mail" value="joeschmoe@one.edu.au" />

              <g:submitButton name="Login as Joe Schmoe" class="btn btn-warning btn-large"/>
            </g:form>
          </div>
          <div class="">
            <h3>Max Mustermann</h3>
            <p>Principal: https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-3</em></strong></p>
              <g:form controller="federatedSessions" action="federatedlogin" method="post">
              <g:hiddenField name="attributes.persistent-id" value="https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-3" />
              <g:hiddenField name="attributes.Shib-Session-ID" value="fake-sessionid-webform" />
              <g:hiddenField name="attributes.Shib-Identity-Provider" value="https://idp.one.edu.au/idp/shibboleth" />
              <g:hiddenField name="attributes.auEduPersonSharedToken" value="TUWr3JO7Zdf1grqyRHiuBXdf-m3" />
              <g:hiddenField name="attributes.cn" value="Max Mustermann" />
              <g:hiddenField name="attributes.mail" value="maxmustermann@one.edu.au" />

              <g:submitButton name="Login as Max Mustermann" class="btn btn-large"/>
            </g:form>
          </div>
          <div class="">
            <h3>Иван Петров</h3>
            <p>Principal: https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-4</em></strong></p>
              <g:form controller="federatedSessions" action="federatedlogin" method="post">
              <g:hiddenField name="attributes.persistent-id" value="https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-4" />
              <g:hiddenField name="attributes.Shib-Session-ID" value="fake-sessionid-webform" />
              <g:hiddenField name="attributes.Shib-Identity-Provider" value="https://idp.one.edu.au/idp/shibboleth" />
              <g:hiddenField name="attributes.auEduPersonSharedToken" value="TUWr3JO7Zdf1grqyRHiuBXdf-m4" />
              <g:hiddenField name="attributes.cn" value="Иван Петров" />
              <g:hiddenField name="attributes.mail" value="i.petrov@one.edu.au" />

              <g:submitButton name="Login as Иван Петров" class="btn btn-danger btn-large"/>
            </g:form>
          </div>
          <div class="">
            <h3>Johnny Moreattr</h3>
            <p>Principal: https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-5</p>
            <g:form controller="federatedSessions" action="federatedlogin" method="post">
              <g:hiddenField name="attributes.persistent-id" value="https://idp.one.edu.au/idp/shibboleth!-!d2404817-6fb9-4165-90d8-5"/>
              <g:hiddenField name="attributes.Shib-Session-ID" value="fake-sessionid-webform" />
              <g:hiddenField name="attributes.Shib-Identity-Provider" value="https://idp.one.edu.au/idp/shibboleth" />
              <g:hiddenField name="attributes.auEduPersonSharedToken" value="TUWr3JO7Zdf1grqyRHiuBXdf-m5" />
              <g:hiddenField name="attributes.cn" value="Johnny Moreattr" />
              <g:hiddenField name="attributes.mail" value="johnnymoreattr@one.edu.au" />

              <g:hiddenField name="attributes.displayName" value="Johnny Johnson Johndel Moreattr" />
              <g:hiddenField name="attributes.assurance" value="urn:mace:aaf.edu.au:iap:id:0" />
              <g:hiddenField name="attributes.unscoped-affiliation" value="member" />
              <g:hiddenField name="attributes.affiliation" value="member@one.edu.au" />
              <g:hiddenField name="attributes.entitlement" value="http://test.aaf.edu.au/entitlements/test" />
              <g:hiddenField name="attributes.o" value="One University" />
              <g:hiddenField name="attributes.authenticationMethod" value="urn:mace:aaf.edu.au:iap:authn:0" />
              <g:hiddenField name="attributes.givenName" value="Johnny" />
              <g:hiddenField name="attributes.sn" value="Moreattr" />
              <g:hiddenField name="attributes.mobileNumber" value="+611234567890" />
              <g:hiddenField name="attributes.telephoneNumber" value="+619876543210" />
              <g:hiddenField name="attributes.postalAddress" value="1 One University St, Onesville" />
              <g:hiddenField name="attributes.organizationalUnit" value="Section One" />
              <g:hiddenField name="attributes.homeOrganization" value="one.edu.au" />
              <g:hiddenField name="attributes.homeOrganizationType" value="urn:mace:terena.org:schac:homeOrganizationType:int:university" />

              <g:submitButton name="Login as Johnny Moreattr" class="btn btn-info btn-large"/>
            </g:form>
          </div>
      </div>

  </body>
</html>
