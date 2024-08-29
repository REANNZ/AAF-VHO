<!doctype html>
<html>
  <head>
    <!-- Contains header, top navigation bar, and footer that is common to many pages-->
    <meta name="layout" content="public" />
    <!-- App-wide requirements-->
    <asset:javascript src="application.js" />
    <asset:stylesheet src="application.css" />
  </head>
  <body>

    <div class="hero-unit">
      <h2>Welcome to the Tuakiri Virtual Home</h2>
      <h3>Which best describes you?</h3>
      <br>
      <div class="row">
        <div class="span5">
          <center><g:img dir="images" file="research.png" alt="End User or Researcher. View your VHR Account." class="img-rounded"/></center>
          <div class="descriptive-text">
            <p>I have a Tuakiri Virtual Home account that lets me access services connected to Tuakiri.</p>
            <p>I'd like to view my account details or change my password.</p>
          </div>
          <p>
            <center><g:link controller="account" action="index" class="btn btn-large btn-success"><i class="icon icon-white icon-user"></i> Login to my account</g:link></center>
          </p>
        </div>

        <div class="span5">
          <center><g:img dir="images" file="operational.png" alt="Operation Staff. Access VHR administration functions." class="img-rounded"/></center>
          <div class="descriptive-text">
            <p>I'm an administrator of Tuakiri Virtual Home Organisations or Groups.</p>
            <p>I'd like to create new accounts or manage existing accounts.</p>
          </div>
          <p>
            <center><g:link controller="dashboard" action="dashboard" class="btn btn-large btn-info"><i class="icon icon-white icon-wrench"></i> Login to the Virtual Home dashboard</g:link><br>
            <small>(You will be asked to authenticate via your Tuakiri Identity Provider)</small></center>
          </p>
        </div>
      </div>
    </div>

    <script type="text/javascript">
      $('.descriptive-text').equalizeCols();
    </script>
  </body>
</html>
