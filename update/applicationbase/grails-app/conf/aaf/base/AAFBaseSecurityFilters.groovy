package aaf.base

import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone
import org.springframework.beans.factory.InitializingBean

class AAFBaseSecurityFilters implements InitializingBean  {

  def grailsApplication
  def developmentAttributesService

  String VALID_REFERER
  String DATE_FORMAT

  void afterPropertiesSet() {
    VALID_REFERER = "^${grailsApplication.config.grails.serverURL}".replace("http", "https?")
    DATE_FORMAT = "EEE, d MMM yyyy HH:mm:ss z"
  }

  def filters = {
    checkReferer(controller: '*', action: '*') {
      before = {
        if (request.method.toUpperCase() != "GET" && request.method.toUpperCase() != "HEAD" && !request.forwardURI.startsWith("/api")) {
          def referer = request.getHeader('Referer')

          if(!(referer && referer =~ VALID_REFERER)) {
            log.error("DENIED - ${request.remoteAddr}|$params.controller/$params.action - Referer: $referer was not valid, should have been a match for $VALID_REFERER")
            response.sendError(403)
            return false
          }
        }
      }
    }

    secureResponse(controller: '*', action: '*') {
      after = {
        Date responseDate = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("GMT"));
        String responseDateHeader = sdf.format(responseDate);

        // We do everything over SSL so prevent caching
        header("Expires", "Tue, 03 Jul 2001 06:00:00 GMT");
        header("Last-Modified", responseDateHeader);
        response.setHeader("Cache-Control", "no-store, no-cache, must-revalidate, max-age=0");
        response.setHeader("Cache-Control", "post-check=0, pre-check=0");
        response.setHeader("Pragma", "no-cache");

        //OWASP - prevent click jacking
        response.addHeader("X-FRAME-OPTIONS", "DENY")
      }
    }

    logout(controller: 'auth', action: '*', exclude:'login') {
      before = {
        accessControl { true }
      }
    }

    workflow_authenticated(uri:"/workflow/**") {
      before = {
        accessControl { true }
      }
    }

    inviteadministrator_authenticated(uri:"/inviteadministrator/**") {
      before = {
        accessControl { true }
      }
    }

    administration_authenticated(uri:"/administration/**") {
      before = {
        accessControl { true }
      }
    }

    administration(uri: "/administration/**") {
      before = {
        if(!accessControl { permission("app:administration") }) {
          log.info("secfilter: DENIED - [${subject.id}]${subject.principal}|${request.remoteAddr}|$params.controller/$params.action")
          response.sendError(403)
          return false
        }
        log.info("secfilter: ALLOWED - [$subject.id]$subject.principal|${request.remoteAddr}|$params.controller/$params.action")
      }
    }

    console_bootstrap(controller:"console") {
      before = {
        if (grailsApplication.config.aaf.base.bootstrap) {
          log.info("secfilter: ALLOWED BOOTSTRAP CONSOLE - ${request.remoteAddr}|$params.controller/$params.action")
          return true
        }

        accessControl { true }
      }
    }

    console(controller:"console") {
      before = {
        if (!grailsApplication.config.aaf.base.bootstrap) {
          if(accessControl { permission("app:administration") }) {
            log.info("secfilter: ALLOWED CONSOLE - [${subject?.id}]${subject?.principal}|${request.remoteAddr}|$params.controller/$params.action")
            return true
          }

          log.info("secfilter: DENIED CONSOLE - [${subject?.id}]${subject?.principal}|${request.remoteAddr}|$params.controller/$params.action")
          response.sendError(404) // Deliberately not 403 so endpoint can't be figured out.
          return false
        }
      }   
    }

    developmentAttributes(controller:'federatedSessions', action:'federatedlogin') {
      before = {
        if (grailsApplication.config.aaf.base.realms.federated.development.active) {
          developmentAttributesService.storeAttributes(request, session, params)
          developmentAttributesService.injectAttributes(request, session)
        }

        true
      }
    }
  }
}
