package aaf.base

import org.codehaus.groovy.grails.commons.GrailsDomainClass
import org.codehaus.groovy.grails.scaffolding.DefaultGrailsTemplateGenerator

class ExtendedGrailsTemplateGenerator extends DefaultGrailsTemplateGenerator {

  public ExtendedGrailsTemplateGenerator(ClassLoader classLoader){
    super(classLoader)
  }

  void generateTest(GrailsDomainClass domainClass, String destDir) {
    File destFile = new File("$destDir/${domainClass.packageName.replace('.','/')}/${domainClass.shortName}ControllerSpec.groovy")
    def templateText = getTemplateText("Spec.groovy")
    def t = engine.createTemplate(templateText)

    def binding = [pluginManager: pluginManager,
    packageName: domainClass.packageName,
    domainClass: domainClass,
    className: domainClass.shortName,
    propertyName: domainClass.logicalPropertyName]

    if (canWrite(destFile)) {
      destFile.parentFile.mkdirs()
      destFile.withWriter {
        t.make(binding).writeTo(it)
      }
    }
  }

  protected boolean canWrite(File testFile) {
    if (!overwrite && testFile.exists()) {
      try {
        def response = GrailsConsole.getInstance().userInput("File ${makeRelativeIfPossible(testFile.absolutePath, basedir)} already exists. Overwrite?",['y','n','a'] as String[])
        overwrite = overwrite || response == "a"
        return overwrite || response == "y"
      }
      catch (Exception e) {
        // failure to read from standard in means we're probably running from an automation tool like a build server
        return true
      }
    }
    return true
  }

  protected String getPropertyName(GrailsDomainClass domainClass) { "${domainClass.propertyName}${domainSuffix}" }

  public String getTemplateText(String template) {
    def templateFile = "./src/templates/scaffolding/${template}"
    return new File(templateFile).getText()
  }
}
