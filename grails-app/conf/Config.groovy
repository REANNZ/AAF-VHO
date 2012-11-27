environments {
  production {
    greenmail.disabled=true
  }
  test {
    greenmail.disabled=false
    grails.mail.default.from="noreply-test@aaf.edu.au"
    grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
  }
  development {
    greenmail.disabled=false
    grails.mail.port = com.icegreen.greenmail.util.ServerSetupTest.SMTP.port
  }
}
grails.views.default.codec="none" // none, html, base64
grails.views.gsp.encoding="UTF-8"