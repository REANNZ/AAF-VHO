<!doctype html>
<html>
  <head>
    <meta name="layout" content="myaccount" />
  </head>
  <body>

    <div class="hero-unit">
      <h2>An email has been sent to this user.</h2>

      <g:render template="/templates/flash" plugin="aafApplicationBase"/>

      <p>Please follow the instructions.</p>

      <h3>Didn't receive an email?</p>

      <p>Click <g:link controller="lostpassword" action="emailed" params="[login:login]">here</g:link> to re-send.</p>
    </div>

  </body>
</html>
