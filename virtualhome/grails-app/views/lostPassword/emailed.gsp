<!doctype html>
<html>
  <head>
    <meta name="layout" content="myaccount" />
  </head>
  <body>

    <div class="hero-unit">
      <h2>If you have an account here, an email has been sent to you.</h2>

      <g:render template="/templates/flash" plugin="aafApplicationBase"/>

      <p>Please follow the instructions in the email.</p>

      <h3>Did not receive the email?</p>

      <p>Click <g:link controller="lostpassword" action="emailed" params="[login:login]">here</g:link> to re-send.</p>
    </div>

  </body>
</html>
