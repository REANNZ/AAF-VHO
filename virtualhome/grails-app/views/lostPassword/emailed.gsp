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

      <h3>Didn't receive the email?</p>

      <g:form action="emailed">
        <g:hiddenField name="login" value="${login}"/>
        <button type="submit" class="btn btn-large">Re-send email.</button>
        <g:link controller="lostpassword"></g:link>
      </g:form>

    </div>

  </body>
</html>
