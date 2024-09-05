import org.apache.commons.io.FileUtils

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsArgParsing")

target(main: "Cleans up a newly created grails app so that we only have bits we want") {
  def grails_dirs = ["i18n", "views"]
  def web_dirs = ["css", "js" ]

  grails_dirs.each { dir ->
    def target = new File("${basedir}/grails-app/${dir}")

    FileUtils.deleteDirectory(target)
    target.mkdir()
  }

  web_dirs.each { dir ->
    def target = new File("${basedir}/web-app/${dir}")

    FileUtils.deleteDirectory(target)
    target.mkdir()
  }
}

setDefaultTarget(main)
