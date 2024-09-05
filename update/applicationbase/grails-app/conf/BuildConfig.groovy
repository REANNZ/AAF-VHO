grails.servlet.version = "3.0"

grails.project.class.dir = "target/classes"
grails.project.test.class.dir = "target/test-classes"
grails.project.test.reports.dir = "target/test-reports"
grails.project.target.level = 1.7
grails.project.source.level = 1.7

grails.project.fork = [
   run: [maxMemory:1024, minMemory:64, debug:false, maxPerm:256]
]

grails.project.dependency.resolution = {
  inherits("global") {
  }

  log "warn"
  checksums true
  
  repositories {
    inherits true

    grailsPlugins()
    grailsHome()
    grailsCentral()

    mavenLocal()
    // mavenCentral()
    mavenRepo "https://repo1.maven.org/maven2"

    mavenRepo "https://repo.grails.org/grails/plugins-releases/"
    mavenRepo "https://download.java.net/maven/2/"
    mavenRepo "https://repository.jboss.org/maven2/"
  }

  dependencies {
    compile "commons-collections:commons-collections:3.2.2"

    test 'mysql:mysql-connector-java:5.1.18'
    test 'org.hamcrest:hamcrest-all:1.3'
    test 'asm:asm:3.3.1'

    runtime 'org.springframework:spring-test:4.1.9.RELEASE'
    runtime 'org.springframework:spring-orm:4.1.9.RELEASE'
    runtime 'org.springframework:spring-tx:4.1.9.RELEASE'
    runtime 'org.springframework:spring-aop:4.1.9.RELEASE'
    runtime 'org.springframework:spring-expression:4.1.9.RELEASE'
    // because resources-1.2.14 does not declare dependency on commons-io
    runtime "commons-io:commons-io:1.3.2"
  }

  /*
    Types of plugin:
    build: Dependencies for the build system only
    compile: Dependencies for the compile step
    runtime: Dependencies needed at runtime but not for compilation (see above)
    test: Dependencies needed for testing but not at runtime (see above)
    provided: Dependencies needed at development time, but not during WAR deployment
  */
  plugins {
    build ":tomcat:7.0.55.2"

    compile ":shiro:1.2.1"
    compile ':cache:1.1.8'
    compile ":mail:1.0.7"
    compile ":greenmail:1.3.4"
    compile ":codenarc:0.24"
    compile ":build-test-data:2.4.0"
    compile ":rest:0.8"
    compile ":scaffolding:2.1.2"

    runtime ":hibernate4:4.3.10"
    runtime ":resources:1.2.14"
    runtime ":zipped-resources:1.0.1"
    runtime ":cached-resources:1.1"
    runtime ":yui-minify-resources:0.1.4"
    runtime ":database-migration:1.4.0"
    runtime ":jquery:1.11.1"
    runtime ":modernizr:2.6.2"
    runtime (":twitter-bootstrap:2.3.2.3") { excludes "svn" }
    runtime ":constraintkeys:0.1"
    runtime ":console:1.5.9"
    runtime ":cache-headers:1.1.7"
    runtime ":audit-logging:1.0.7"

    test    ":code-coverage:1.2.5"
  }
}

grails.project.dependency.resolver = "maven"

codenarc {
  properties = { GrailsPublicControllerMethod.enabled = false
                 ThrowRuntimeException.enabled = false }
}

coverage {
  exclusions = ["**/AAFBaseConfig*"]
  sourceInclusions = ['grails-app/realms']
}
