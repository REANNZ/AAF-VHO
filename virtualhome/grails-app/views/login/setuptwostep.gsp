<!doctype html>
<html>
  <head>
    <meta name="layout" content="myaccount" />
  </head>
  <body>

    <div class="row">
      <div class="span12">
        <h2>Hello <g:fieldValue bean="${managedSubjectInstance}" field="cn"/></h2>
        <h3 class="muted">There is an important change required for your account</h3>
      </div>
    </div>

    <div class="row">
      <div class="span12">
        <p>The AAF have been busy to make your account and access to AAF services even more secure. The Virtual Home now supports 2-Step verification which is a secondary security measure used when logging into your account. The <strong>administrators of your account</strong> have enabled this for you, so simply follow the 3 steps below to set up and enable 2-Step verification to continue accessing AAF services.</p>

        <g:render template="/templates/appdetails" />

        <p><g:link action="completesetuptwostep" class="btn btn-large btn-warning">I'm ready - Enable 2-Step verification</g:link></p>
      </div>
    </div>

    <hr>

    <h3 class="muted">Getting Help</h3>
    <p>If you have any queries about this change to your account please contact one of the administrators shown below.</p>
    <g:render template="/templates/accountadministrators"/>

  </body>
</html>
