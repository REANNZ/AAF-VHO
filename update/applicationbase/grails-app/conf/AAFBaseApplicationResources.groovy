modules = { 
  overrides {
    'modernizr' {
      defaultBundle 'app_base'
    }
    'bootstrap' {
      defaultBundle 'app_base'
    }
  }
  'bootstrap-datepicker' {
    // Source: http://www.eyecon.ro/bootstrap-datepicker/
    defaultBundle 'app_base'

    dependsOn 'bootstrap'
    resource url: [plugin: 'aafApplicationBase', dir:'js/bootstrap', file:'bootstrap-datepicker.min.js'], attrs:[type:'js']
    resource url: [plugin: 'aafApplicationBase', dir:'css/bootstrap-datepicker.css'], attrs:[type:'css']
  }
  'bootstrap-notify' {
    // Source: http://nijikokun.github.com/bootstrap-notify/
    defaultBundle 'app_base'

    dependsOn 'bootstrap'
    resource url: [plugin: 'aafApplicationBase', dir:'js/bootstrap', file:'bootstrap-notify-1.0.min.js'], attrs:[type:'js']
    resource url: [plugin: 'aafApplicationBase', dir:'css/bootstrap-notify-1.0.css'], attrs:[type:'css']
  }
  'bootstrap-multiselect' {
    // Source: https://github.com/davidstutz/bootstrap-multiselect
    defaultBundle 'app_base'

    dependsOn 'bootstrap'
    resource url: [plugin: 'aafApplicationBase', dir:'js/bootstrap', file:'bootstrap-multiselect.min.js'], attrs:[type:'js']
  }
  'bootbox' {
    // Source: http://bootboxjs.com/
    defaultBundle 'app_base'

    dependsOn 'bootstrap'
    resource url: [plugin: 'aafApplicationBase', dir:'js/bootstrap', file:'bootbox.min.js']  , attrs:[type:'js']
  }
  'less' {
    // Source: http://lesscss.org/
    defaultBundle false

    resource url: [plugin: 'aafApplicationBase', dir:'js/less-1.3.0.min.js'], disposition: 'head'
  }
  'validate' {
    // Source: http://bassistance.de/jquery-plugins/jquery-plugin-validation/
    dependsOn 'jquery'
    defaultBundle false

    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.validate-1.10.0.min.js'], attrs:[type:'js']
    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.validate.additional-1.10.min.js'], attrs:[type:'js']
  }
  'datatables' {
    // Source: http://datatables.net/

    dependsOn 'jquery' 
    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.datatables-1.9.3.min.js'], disposition: 'head', nominify: true, attrs:[type:'js']
    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.datatables.bootstrap-1.9.3.min.js'], disposition: 'head', nominify: true, attrs:[type:'js']
  }
  'formrestrict' {
    // Source: https://github.com/treyhunner/jquery-formrestrict
    defaultBundle 'app_base'

    dependsOn 'jquery'
    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.alphanumeric.min.js'], attrs:[type:'js']
    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.formrestrict.min.js'], attrs:[type:'js']
  }
  'highcharts' {
    defaultBundle 'app_base'

    resource url: [plugin: 'aafApplicationBase', dir:'js/highcharts-2.3.2.min.js'], attrs:[type:'js']
  }
  'codemirror' {
    // Source: http://codemirror.net
    resource url: [plugin: 'aafApplicationBase', dir:'/js/codemirror', file:'codemirror.min.js'], disposition:'head', nominify: true
  }
  'equalizecols' {
    // Source: http://tomdeater.com/jquery/equalize_columns/
    defaultBundle 'app_base'

    dependsOn 'jquery'
    resource url: [plugin: 'aafApplicationBase', dir:'js/jquery', file:'jquery.equalizecols.min.js'], attrs:[type:'js'] 
  }
  'app_base' {
    defaultBundle 'app_base'

    dependsOn ['jquery', 'less', 'bootstrap']
    resource url: [plugin: 'aafApplicationBase', dir:'js/aaf_base_application.js'], attrs:[type:'js']
    resource url: [plugin: 'aafApplicationBase', dir:'css/aaf_base_application.css'], attrs:[rel:'stylesheet/less', type:'css']
  }
}
