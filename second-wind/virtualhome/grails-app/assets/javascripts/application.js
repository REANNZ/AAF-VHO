// This is a manifest file that'll be compiled into application.js.
//
// Any JavaScript file within this directory can be referenced here using a relative path.
//
// You're free to add application-wide JavaScript to this file, but it's generally better
// to create separate JavaScript files as needed.
//
//= require jquery-2.2.0.min
//= require bootstrap
//= require_tree .
//= require_self
// Include custom scripts through the asset pipeline
//= require aaf_base_application
//= require bootstrap/bootstrap-datepicker.min
//= require bootstrap/bootstrap-notify-1.0.min
//= require bootstrap/bootstrap-multiselect.min
//= require bootstrap/bootbox.min
//= require jquery/jquery.validate-1.10.0.min
//= require jquery/jquery.validate.additional-1.10.min
//= require jquery/jquery.datatables-1.9.3.min
//= require jquery/jquery.datatables.bootstrap-1.9.3.min
//= require jquery/jquery.alphanumeric.min
//= require jquery/jquery.formrestrict.min
//= require highcharts-2.3.2.min
//= require jquery/jquery.equalizecols.min

if (typeof jQuery !== 'undefined') {
	(function($) {
		$('#spinner').ajaxStart(function() {
			$(this).fadeIn();
		}).ajaxStop(function() {
			$(this).fadeOut();
		});
	})(jQuery);
}

function exportTableToCSV($table, filename) {
    // Adapted from http://jsfiddle.net/terryyounghk/KPEGU/
    var $rows = $table.find('tr:has(td),tr:has(th)'),

    // Temporary delimiter characters unlikely to be typed by keyboard
    // This is to avoid accidentally splitting the actual contents
    tmpColDelim = String.fromCharCode(11), // vertical tab character
    tmpRowDelim = String.fromCharCode(0), // null character

    // actual delimiter characters for CSV format
    colDelim = ',',
    rowDelim = '\r\n',

    // Grab text from table into CSV formatted string
    csv = $rows.map(function (i, row) {
        var $row = $(row),
            $cols = $row.find('td,th').not('.exportable-exclude');

        return $cols.map(function (j, col) {
            var $col = $(col),
                text = $col.text();

            return text.replace('"', '""'); // escape double quotes

        }).get().join(tmpColDelim);
    }).get().join(tmpRowDelim)
        .split(tmpRowDelim).join(rowDelim)
        .split(tmpColDelim).join(colDelim);

    var link = document.createElement("a");
    if(link.download !== undefined) {   // Sane browser or some IE-ish crap?
      $(this)
          .attr({
          'download': filename,
              'href': 'data:application/csv;charset=utf-8,' + encodeURIComponent(csv),
              'target': '_blank'
      });
    } else {
      if(navigator.msSaveBlob) {
        var blob = new Blob([csv], {"type": "text/csv;charset=utf-8;"});
        navigator.msSaveBlob(blob, fileName);
      }
    }
};

$(document).on('click', '.show-add-administrative-members', function() {
  var btn = $(this);
  var form = btn.next('form');
  $.ajax({
    type: "POST",
    cache: false,
    url: form.attr('action'),
    data: form.serialize(),
    success: function(res) {
      $('a[href="#tab-administrators"]').tab('show'); // Select tab by name

      var target = $("#add-administrative-members");
      target.html(res);
      aaf_base.applyBehaviourTo(target);
      target.fadeIn();
    },
    error: function (xhr, ajaxOptions, thrownError) {
      aaf_base.reset_button(btn);
      aaf_base.popuperror();
    }
  });
});

$(document).on('click', '.add-administrative-member', function() {
  aaf_base.set_button($(this));
  var btn = $(this);
  var form = btn.parent();
  $.ajax({
    type: "POST",
    cache: false,
    url: form.attr('action'),
    data: form.serialize(),
    success: function(res) {
      var target = $("#administrative-members");
      target.html(res);
      aaf_base.applyBehaviourTo(target);
      target.fadeIn();

      aaf_base.reset_button(btn);
    },
    error: function (xhr, ajaxOptions, thrownError) {
      aaf_base.reset_button(btn);
      aaf_base.popuperror();
    }
  });
});

$(document).on('submit', '#invite-administrative-member', function() {
  var form = $(this);
  var btn = $(':submit', this);
  aaf_base.set_button(btn);
  $.ajax({
    type: "POST",
    cache: false,
    url: form.attr('action'),
    data: form.serialize(),
    success: function(res) {
      var target = $("#administrative-members");
      target.html(res);
      aaf_base.applyBehaviourTo(target);
      target.fadeIn();

      aaf_base.reset_button(btn);
    },
    error: function (xhr, ajaxOptions, thrownError) {
      aaf_base.reset_button(btn);
      aaf_base.popuperror();
    }
  });

  return false;
});

$(document).on('click', '.remove-administrative-member', function() {
  aaf_base.set_button($(this));
  var btn = $(this);
  var form = btn.parent();
  $.ajax({
    type: "POST",
    cache: false,
    url: form.attr('action'),
    data: form.serialize(),
    success: function(res) {
      var target = $("#administrative-members");
      target.html(res);
      aaf_base.applyBehaviourTo(target);
      target.fadeIn();

      aaf_base.reset_button(btn);
    },
    error: function (xhr, ajaxOptions, thrownError) {
      aaf_base.reset_button(btn);
      aaf_base.popuperror();
    }
  });
});

$(document).on('click', '.export', function() {
  exportTableToCSV.apply(this, [$('.exportable-table'), "aaf-vh-export.csv"]);
});

jQuery(function($) {
  $.validator.addMethod("notEqual", function(value, element, param) {
    return value != $(param).val();
  }, "This must be a different value");

  $.validator.addClassRules("resetCodeExternal", {
    notEqual: '#resetCode'
  });
});