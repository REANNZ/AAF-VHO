import grails.util.BuildSettings
import grails.util.Environment
import org.springframework.boot.logging.logback.ColorConverter
import org.springframework.boot.logging.logback.WhitespaceThrowableProxyConverter

import java.nio.charset.Charset
import ch.qos.logback.classic.encoder.PatternLayoutEncoder
import ch.qos.logback.core.FileAppender
import ch.qos.logback.core.ConsoleAppender
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy

conversionRule 'clr', ColorConverter
conversionRule 'wex', WhitespaceThrowableProxyConverter

// Define the pattern layout encoder
def patternEncoder = { pattern ->
    def encoder = new PatternLayoutEncoder()
    encoder.pattern = pattern
    encoder.context = context
    encoder.start()
    encoder
}

// Define the rolling policy
def rollingPolicy = { filename ->
    def policy = new TimeBasedRollingPolicy()
    policy.fileNamePattern = filename
    policy.context = context
    policy.start()
    policy
}

// Define appenders
appender("FILE_TEST_OUTPUT", FileAppender) {
    file = "/tmp/app-test-output.log"
    encoder = patternEncoder("%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n")
}

appender("DAILY_ROLLING_AAFBASE_SECURITY", FileAppender) {
    file = "/tmp/aafbase-security.log"
    rollingPolicy = rollingPolicy("/tmp/aafbase-security.log.%d{yyyy.mm.dd}")
    encoder = patternEncoder("%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n")
}

appender("DAILY_ROLLING_AAFBASE", FileAppender) {
    file = "/tmp/aafbase.log"
    rollingPolicy = rollingPolicy("/tmp/aafbase.log.%d{yyyy.mm.dd}")
    encoder = patternEncoder("%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n")
}

appender("DAILY_ROLLING_AAFBASE_GRAILS", FileAppender) {
    file = "/tmp/aafbase-grails.log"
    rollingPolicy = rollingPolicy("/tmp/aafbase-grails.log.%d{yyyy.mm.dd}")
    encoder = patternEncoder("%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n")
}

appender("DAILY_ROLLING_STACKTRACE", FileAppender) {
    file = "/tmp/aafbase-stacktrace.log"
    rollingPolicy = rollingPolicy("/tmp/aafbase-stacktrace.log.%d{yyyy.mm.dd}")
    encoder = patternEncoder("%d{[ dd.MM.yy HH:mm:ss.SSS]} %-5p %c %x - %m%n")
}

// Console appender configuration
appender('STDOUT', ConsoleAppender) {
    encoder(PatternLayoutEncoder) {
        charset = Charset.forName('UTF-8')

        pattern =
                '%clr(%d{yyyy-MM-dd HH:mm:ss.SSS}){faint} ' + // Date
                        '%clr(%5p) ' + // Log level
                        '%clr(---){faint} %clr([%15.15t]){faint} ' + // Thread
                        '%clr(%-40.40logger{39}){cyan} %clr(:){faint} ' + // Logger
                        '%m%n%wex' // Message
    }
}

// Runtime configuration for development mode
def targetDir = BuildSettings.TARGET_DIR
if (Environment.isDevelopmentMode() && targetDir != null) {
    appender("FULL_STACKTRACE", FileAppender) {
        file = "${targetDir}/stacktrace.log"
        append = true
        encoder(PatternLayoutEncoder) {
            pattern = "%level %logger - %msg%n"
        }
    }
    logger("StackTrace", ERROR, ['FULL_STACKTRACE'], false)
}

// Define loggers
logger("grails.buildtestdata", WARN, ["FILE_TEST_OUTPUT"], false)
logger("grails.app.controllers", INFO, ["DAILY_ROLLING_AAFBASE"], false)
logger("grails.app.domains", INFO, ["DAILY_ROLLING_AAFBASE"], false)
logger("grails.app.services", INFO, ["DAILY_ROLLING_AAFBASE"], false)
logger("grails.app.realms", INFO, ["DAILY_ROLLING_AAFBASE"], false)
logger("aaf.vhr", INFO, ["DAILY_ROLLING_AAFBASE"], false)
logger("org.apache.shiro", INFO, ["DAILY_ROLLING_AAFBASE"], false)
logger("org.spockframework", INFO, ["DAILY_ROLLING_AAFBASE"], false)

logger("org.codehaus.groovy.grails.web.servlet", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)
logger("org.codehaus.groovy.grails.web.pages", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)
logger("org.codehaus.groovy.grails.web.sitemesh", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)
logger("org.codehaus.groovy.grails.web.mapping.filter", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)
logger("org.codehaus.groovy.grails.web.mapping", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)
logger("org.codehaus.groovy.grails.commons", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)
logger("org.codehaus.groovy.grails.plugins", WARN, ["DAILY_ROLLING_AAFBASE_GRAILS"], false)

// Define the root logger
root(INFO, ["DAILY_ROLLING_AAFBASE"])