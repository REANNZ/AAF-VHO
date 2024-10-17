import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.rolling.RollingFileAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

import grails.util.Environment

// Port of the 'app-security' appender from application_config.grooby
appender('app-security-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app-security.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'app' appender from application_config.groovy
appender('app-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'app-grails' appender from application_config.groovy
appender('app-grails-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app-grails.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'stacktrace' appender from application_config.groovy
appender('stacktrace-appender', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/opt/virtualhome/logs/app-stacktrace.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'aafbase-security' appender from Config.groovy in applicationbase
appender('aafbase-security', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/tmp/aafbase-security.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'aafbase' appender from Config.groovy in applicationbase
appender('aafbase', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/tmp/aafbase.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'aafbase-grails' appender from Config.groovy in applicationbase
appender('aafbase-grails', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/tmp/aafbase-grails.log-%d{yyyy-MM-dd}"
    }
}

// Port of the 'stacktrace' appender from Config.groovy in applicationbase
appender('stacktrace', RollingFileAppender) {
    append = true
    encoder(PatternLayoutEncoder) {
        pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
    }
    rollingPolicy(TimeBasedRollingPolicy) {
        FileNamePattern = "/tmp/aafbase-stacktrace.log-%d{yyyy-MM-dd}"
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
logger("org.grails.web.servlet", WARN, ['app-grails-appender'], false)
logger("grails.web.pages", WARN, ['app-grails-appender'], false)
//logger("org.codehaus.groovy.grails.web.sitemesh", WARN, ['app-grails-appender'], false)
//logger("org.codehaus.groovy.grails.web.mapping.filter", WARN, ['app-grails-appender'], false)
logger("org.grails.web.mapping", WARN, ['app-grails-appender'], false)
logger("org.grails.commons", WARN, ['app-grails-appender'], false)
logger("grails.plugins", WARN, ['app-grails-appender'], false)


if(Environment.current == Environment.TEST) {
    // Port of the 'test-output' appender from Config.groovy.
    appender('test-output-appender', FileAppender) {
        file = "/tmp/app-test-output.log"
        append = false
        encoder(PatternLayoutEncoder) {
            pattern = "%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %X - %m%n"
        }
    }

    // Port of the 'test-output' logger from Config.groovy
    logger('grails.buildtestdata', WARN, ['test-output-appender'], false)
    logger('grails.app.controllers', INFO, ['test-output-appender'], true)
    logger('grails.app.domains', INFO, ['test-output-appender'], true)
    logger('grails.app.services', INFO, ['test-output-appender'], true)
    logger('grails.app.realms', INFO, ['test-output-appender'], true)
    logger('aaf.vhr', INFO, ['test-output-appender'], true)

    // Port of the 'aafbase-security' logger from Config.groovy in applicationbase
    logger('grails.app.filters', INFO, ['aafbase-security'], false)

    // Port of the 'aafbase' logger from Config.groovy in applicationbase
    logger('grails.app.controllers', DEBUG, ['aafbase'], false)
    logger('grails.app.domains', DEBUG, ['aafbase'], false)
    logger('grails.app.services', DEBUG, ['aafbase'], false)
    logger('grails.app.realms', DEBUG, ['aafbase'], false)
    logger('aaf.base', DEBUG, ['aafbase'], false)
    logger('org.apache.shiro', DEBUG, ['aafbase'], false)
    logger('org.spockframework', DEBUG, ['aafbase'], false)

    logger('org.grails.web.servlet', WARN, ['aafbase-grails'], false)
    logger('grails.web.pages', WARN, ['aafbase-grails'], false)
    //logger('org.codehaus.groovy.grails.web.sitemesh', WARN, ['aafbase-grails'], false)
    //logger('org.codehaus.groovy.grails.web.mapping.filter', WARN, ['aafbase-grails'], false)
    logger('org.grails.web.mapping', WARN, ['aafbase-grails'], false)
    logger('org.grails.commons', WARN, ['aafbase-grails'], false)
    logger('grails.plugins', WARN, ['aafbase-grails'], false)
}
