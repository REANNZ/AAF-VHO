import org.apache.commons.io.FileUtils

includeTargets << grailsScript("Init")
includeTargets << grailsScript("_GrailsArgParsing")

target(main: "Populates templates for scaffolding generation") {
  def source = "${aafApplicationBasePluginDir}"
  def dest = "${basedir}"

  def dst = new File("${dest}/src/templates")
  def src = new File("${source}/src/branding/src/templates")

  FileUtils.copyDirectory(src, dst)
}

setDefaultTarget(main)
