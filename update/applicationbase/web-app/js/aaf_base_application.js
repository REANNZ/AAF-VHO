window.aaf_base = window.aaf_base || {};
var aaf_base = window.aaf_base;

$(function() {
  aaf_base.applyBehaviourTo(document);

  var url = document.location.toString();
  if (url.match('#')) {
    $('.nav-tabs a[href=#'+url.split('#')[1]+']').tab('show') ;
  }

  $('a.delete-ensure').click(function(e) {
    var btn = $(this)
    e.preventDefault();
    bootbox.dialog(btn.data('confirm'), [{
      'label': "Cancel",
      'class': "btn"
    }, {
      'label': "Delete",
      'class': "btn-danger",
      'callback': function() {
        btn.next('form').submit();
      }
    }], {
      'header': 'Confirm Delete'
    });
  });

  $('a.form-link-submitter').click(function(e) {
    $(this).next('form').submit();
  });

  $('a.archive-ensure').click(function(e) {
    var btn = $(this)
    e.preventDefault();
    bootbox.dialog(btn.data('confirm'), [{
      'label': "Cancel",
      'class': "btn"
    }, {
      'label': "Archive",
      'class': "btn-warning",
      'callback': function() {
        btn.next('form').submit();
      }
    }], {
      'header': 'Confirm Archive'
    });
  });

  $('a.unarchive-ensure').click(function(e) {
    var btn = $(this)
    e.preventDefault();
    bootbox.dialog(btn.data('confirm'), [{
      'label': "Cancel",
      'class': "btn"
    }, {
      'label': "Unarchive",
      'class': "btn-warning",
      'callback': function() {
        btn.next('form').submit();
      }
    }], {
      'header': 'Confirm Unarchive'
    });
  });

  $('a.form-link-submitter').click(function(e) {
    $(this).next('form').submit();
  });
});

aaf_base.applyBehaviourTo = function(scope) {
  $("[rel=twipsy]", scope).tooltip({offset:3}); 
  $("[rel=tooltip]", scope).tooltip({offset:3}); 

  $('.form-validating', scope).validate({
    ignore: ":disabled",
    keyup: true,
    focusout: false,
    focusInvalid:true,
    errorElement: "div",
    wrapper: "div",
    errorPlacement: function(error, element) {
      error.css('margin-bottom', '12px');

      error.addClass('text-error');
      element.parent().append(error);
    }
  });

  $('.revealable', scope).hide();

  $('.table-sortable', scope).dataTable( {
    "sDom": "<'row'<'span6 hidden-phone'l><'span6'f>r><'row'<'span12't>><'row'<'span5 hidden-phone'i><'span7'p>>",
    "sPaginationType": "bootstrap",
    "bLengthChange": true,
    "bAutoWidth": true,
    "oLanguage": {
      "sLengthMenu": "show _MENU_ rows"
    }
  });
};

aaf_base.sessionTimeoutMonitor = function(timeout, decisionTimeout, content, pollURL, logoutURL) {
  window.setInterval(aaf_base.sessionTimeoutWarning, timeout, timeout, decisionTimeout, content, pollURL, logoutURL);    
};

aaf_base.sessionTimeoutWarning = function(timeout, decisionTimeout, content, pollURL, logoutURL) {
  var box = bootbox.dialog(content, [{
    'label': "Logout",
    'class': "btn",
    'callback': function() {
      window.location.replace(logoutURL);
    }
  }, {
    'label': "Continue Session",
    'class': "btn btn-success",
    'callback': function() {
      $.get(pollURL);
      clearTimeout(decision_timer);
      return true;
    }
  }], {
    'keyboard' : false,
    'backdrop': 'static'
  });

  var decision_timer = setTimeout(function() {
    window.location.replace(logoutURL);
  }, decisionTimeout);
};

aaf_base.set_button = function(b) {
  btn = b; 
  btn.button('loading');
  $('.btn').attr('disabled', '');
};

aaf_base.reset_button = function() {
  $('.btn').button('reset').removeAttr('disabled');
};

aaf_base.popuperror= function() {
  $('.top-right').notify({type:'error', closable:true, 
    message: { html: '<b>Error</b><br>An error occured communicating with the server.<br>This has been logged for review.<br>If you continue to have problems please <b>contact support</b>.' },
    fadeOut: { enabled: true, delay: 10000 },
    }).show();
};

$(document).on('click', '.ajax-modal', function() {
  $.get($(this).attr('data-load'),function(d){
    bootbox.dialog(d, [{
      'label': "Ok",
      'class': "btn btn-success"
    }]);
  });
});

// Administration
$(document).on('click', '#show-add-role-members', function() {
  aaf_base.set_button($(this));
  var btn = $(this);
  $.ajax({
    type: "GET",
    cache: false,
    url: searchNewMembersEndpoint,
    success: function(res) {
      var target = $("#add-role-members");
      target.html(res);
      aaf_base.applyBehaviourTo(target);
      target.fadeIn();
      
      aaf_base.reset_button(btn);
      btn.hide();     
    },
    error: function (xhr, ajaxOptions, thrownError) {
      aaf_base.reset_button(btn);
      aaf_base.popuperror();
    }
  });
});

aaf_base.administration_dashboard_sessions_report = function(sessions) {
  var chart;
  var d = new Date();
  d.setMonth(d.getMonth() - 11);

  var data = $.map(sessions, function(value) {
    var dataPoint = { x: d.getTime(), y: value }
    d.setUTCMonth(d.getUTCMonth() + 1);
    return dataPoint;
  });

  chart = new Highcharts.Chart({
    chart: {
      renderTo: 'sessionschart',
      type: 'spline'
    },
    title: {
      text: ''
    },
    xAxis: {
      type: 'datetime',
      dateTimeLabelFormats: { // don't display the dummy year
        month: '%b  \'%y',
        year: '%b'
      }
    },
    yAxis: {
      title: {
        text: 'Sessions'
      },
      min: 0,
    },
    plotOptions: {
      area: {
        marker: {
          enabled: false,
          symbol: 'circle',
          radius: 2,
          states: {
            hover: {
              enabled: false
            }
          }
        }
      }
    },
    tooltip: {
      enabled: false,
    },
    series: [{
      name: ' ',
      showInLegend: false,
      data: data,
    }]
  });
};
