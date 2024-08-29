<!doctype html>
<html>
  <head>
    <meta name="layout" content="public" />
    <!-- JQuery plugins-->
    <asset:javascript src="jquery-2.2.0.min.js" />
    <asset:javascript src="jquery.equalizecols.min.js" />
  </head>
  <body>

    <div class="hero-unit">
        <div class="descriptive-text">
          <p><g:message encodeAs='HTML' code="branding.application.name"/></p>
          <p><g:message encodeAs='HTML' code="branding.application.development.name"/></p>
        </div>
    </div>

    <script type="text/javascript">
      $('.descriptive-text').equalizeCols();
    </script>
  </body>
</html>
