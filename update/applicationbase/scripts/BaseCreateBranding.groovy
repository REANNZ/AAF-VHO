import org.apache.commons.io.FileUtils

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsArgParsing")

target(main: "Populates initial branding for customisation") {
  def source = "${aafApplicationBasePluginDir}"
  def dest = "${basedir}"

  def dirs = [ "grails-app",
                "web-app" ]

  dirs.each { dir ->
    def dst = new File("${dest}/${dir}")
    def src = new File("${source}/src/branding/${dir}")

    FileUtils.copyDirectory(src, dst)
  }

  // Promote our config
  FileUtils.copyFile(new File("${source}/grails-app/conf/AAFBaseConfig.groovy"), new File("${dest}/grails-app/conf/Config.groovy"))
  FileUtils.copyFile(new File("${source}/src/branding/application_config.groovy.orig"), new File("${dest}/application_config.groovy.orig"))
  FileUtils.copyFile(new File("${source}/src/branding/application_config.groovy.orig"), new File("${dest}/application_config.groovy"))

  // Default .gitignore
  FileUtils.copyFile(new File("${source}/.gitignore"), new File("${dest}/.gitignore"))
}

setDefaultTarget(main)
