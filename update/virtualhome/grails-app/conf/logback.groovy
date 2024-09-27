import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

// Port of the 'app-security' appender from application_config.grooby
appender('app-security-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app-security.log-yyyy-MM-dd"
    }
}

// Port of the 'app' appender from application_config.groovy
appender('app-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app.log-yyyy-MM-dd"
    }
}

// Port of the 'app-grails' appender from application_config.groovy
appender('app-grails-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app-grails.log-yyyy-MM-dd"
    }
}

// Port of the 'stacktrace' appender from application_config.groovy
appender('stacktrace-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app-stacktrace.log-yyyy-MM-dd"
    }
}

// Port of the 'app-security' logger
logger('grails.app.filters', INFO, ['app-security-appender'], false)

// Port of the 'app' logger
logger("grails.app.controllers", INFO, ['app-appender'], false)
logger("grails.app.domains", INFO, ['app-appender'], false)
logger("grails.app.services", INFO, ['app-appender'], false)
logger("grails.app.realms", INFO, ['app-appender'], false)
logger("aaf.vhr", INFO, ['app-appender'], false)
logger("org.apache.shiro", INFO, ['app-appender'], false)

// Port of the 'app-grails' logger
logger("org.codehaus.groovy.grails.web.servlet", WARN, ['app-grails-appender'], false)
logger("org.codehaus.groovy.grails.web.pages", WARN, ['app-grails-appender'], false)
logger("org.codehaus.groovy.grails.web.sitemesh", WARN, ['app-grails-appender'], false)
logger("org.codehaus.groovy.grails.web.mapping.filter", WARN, ['app-grails-appender'], false)
logger("org.codehaus.groovy.grails.web.mapping", WARN, ['app-grails-appender'], false)
logger("org.codehaus.groovy.grails.commons", WARN, ['app-grails-appender'], false)
logger("org.codehaus.groovy.grails.plugins", WARN, ['app-grails-appender'], false)

environments {
    test {
        // Port of the 'test-output' appender from Config.groovy.
        appender('test-output-appender', FileAppender) {
            file = "/tmp/app-test-output.log"
            append = false
            encoder(PatternLayoutEncoder) {
                pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n"
            }
        }

        // Port of the 'test-output' logger from Config.groovy
        logger('grails.buildtestdata', WARN, ['test-output-appender'], false)
        logger('grails.app.controllers', INFO, ['test-output-appender'], true)
        logger('grails.app.domains', INFO, ['test-output-appender'], true)
        logger('grails.app.services', INFO, ['test-output-appender'], true)
        logger('grails.app.realms', INFO, ['test-output-appender'], true)
        logger('aaf.vhr', INFO, ['test-output-appender'], true)
    }
}
