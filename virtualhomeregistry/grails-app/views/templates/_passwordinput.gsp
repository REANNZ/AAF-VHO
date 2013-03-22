<div class="control-group">
  <label class="control-label" for="plainPassword">New Password</label>
  <div class="controls">
    <div class="span5">
      <input class="span5 required" id="plainPassword" name="plainPassword" type="password" autocomplete="off" data-typetoggle="#showpassword">
      <input id="showpassword" type="checkbox" tabindex="-1"/> <span>unmask</span>
      <p class="lead" style="margin-top:12px; margin-bottom: 6px;">Your password is <strong id="pwlength">0</strong> characters long</p>
    </div>
    <div class="span4">
      <div class="help-block">
        <p>The AAF has strict password requirements in order to meet international security standard NIST 800-63.</p>

        <p>The AAF <a href="http://xkcd.com/936/" target="_blank" tabindex="-1">and others</a> recommend you use a password/passphrase of <strong>greater then 16 characters</strong>. A phrase unique to you is the easiest way to achieve this: e.g. <em>'IbackedHorsenumber46intheMelbournecup!' or 'ilovedvisitingIcelandin2012'</em>. While not required capitalisation and non alphabetic characters as shown make your phrase even better.</p>

        <p>Often a longer passphrase is actually easier to remember then a password with fewer characters but stricter requirements like the ones below.</p>

        <p>If you wish to use a password of less than 16 characters in length the following requirements <strong>apply</strong>:</p>
        <ul>
          <li>Minimum <strong>length of 8 characters</strong>;</li>
          <li>Does not contain your username;</li>
          <li>At least 1 number;</li>
          <li>At least 1 symbol e.g. !,@,$ etc;</li>
          <li>At least 1 uppercase character;</li>
          <li>At least 1 lwercase chatacter;</li>
          <li>No dictionary words;</li>
          <li>No whitespace;</li>
          <li>No alphabetic sequences;</li>
          <li>No numerical sequences;</li>
          <li>No QWERTY sequences; and</li>
          <li>No more than 3 repeat characters.</li>
        </ul>
      </div>
    </div>
  </div>
</div>

<div class="control-group">
  <label class="control-label" for="plainPasswordConfirmation">Confirm New Password</label>
  <div class="controls">
    <div class="span5">
      <input class="span5 required" name="plainPasswordConfirmation" id="plainPasswordConfirmation" type="password" autocomplete="off" data-typetoggle="#showpassword">
    </div>
    <div class="span4">
      <span class="help-block">Please re-type your password so we can make sure you didn't make a mistake</span>
    </div>
  </div>
</div>

<r:script>
  $('#plainPassword').showPassword();
  $('#plainPasswordConfirmation').showPassword();

  var examples = ['lol', 'correcthorsebatterystaple', 'ilovedvisitingIcelandin2012', 'IbackedHorsenumber46intheMelbournecup!'];

  $('#plainPassword').on("keyup", function() {
    $('#pwlength').html($('#plainPassword').val().length);
    
  });

  $('button[type=submit]').click(function() {
    if($("#accountform").valid()) {
      aaf_base.set_button($(this));
      $(this).parents('form').submit();
    }
  });

  jQuery.validator.addMethod("notAnExample", function(value, element) {
    if(jQuery.inArray($('#plainPassword').val().trim(),examples) != -1) {
      return false;
    } else {
      return true;
    }
  }, "");

  jQuery.validator.addMethod("noSpace", function(value, element) { 
    return value.indexOf(" ") < 0 && value != ""; 
  }, "Spaces are not permitted.");

  $("#accountform").validate({
    rules: {
      login: {
        required: true,
        minlength: 3,
        noSpace:true,
        remote: { url:"login-available", async:true }
      },
      plainPassword: {
        minlength: 8,
        notAnExample: true
      },
      plainPasswordConfirmation: {
        minlength: 8,
        equalTo: '#plainPassword'
      }
    },
    errorElement: "div",
    wrapper: "div",
    errorPlacement: function(error, element) {
      error.css('margin-bottom', '12px');

      error.addClass('text-error');
      element.parent().append(error);

      if(!$('#loginavailable').hasClass('hidden')) {
        $('#loginavailable').addClass('hidden');
      }
    },
    messages:{
      login: {
        remote: 'Unfortunately <strong>this username is taken</strong>, please choose something unique.'
      },
      plainPassword: {
        notAnExample: '<strong>LOL!!!</strong> not a good idea to use the examples :)'
      },
      plainPasswordConfirmation: {
        equalTo: 'Oops! Your passwords do not quite match.'
      }
    },
    keyup: true,
    focusout: false,
    focusInvalid:true
  });
</r:script>
