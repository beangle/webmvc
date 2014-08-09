package org.beangle.webmvc.view.freemarker

import java.io.{ File, IOException }

import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{ split, substringAfter }

import freemarker.cache.{ FileTemplateLoader, MultiTemplateLoader, TemplateLoader, WebappTemplateLoader }
import javax.servlet.ServletContext

object Configurations {

  def disableFreemarkerLogger(): Unit = {
    try {
      freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE)
    } catch {
      case t: Exception => t.printStackTrace()
    }
  }
  /**
   * The default template loader is a MultiTemplateLoader which includes
   * BeangleClassTemplateLoader(classpath:) and a WebappTemplateLoader
   * (webapp:) and FileTemplateLoader(file:) . All template path described
   * in init parameter templatePath or TemplatePlath
   * <p/>
   * The ClassTemplateLoader will resolve fully qualified template includes that begin with a slash.
   * for example /com/company/template/common.ftl
   * <p/>
   * The WebappTemplateLoader attempts to resolve templates relative to the web root folder
   */
  def createTemplateLoader(sc: ServletContext, templatePath: String): TemplateLoader = {
    val paths: Array[String] = split(templatePath, ",")
    val loaders = new collection.mutable.ListBuffer[TemplateLoader]
    for (path <- paths) {
      if (path.startsWith("class://")) {
        loaders += new BeangleClassTemplateLoader(substringAfter(path, "class://"))
      } else if (path.startsWith("file://")) {
        try {
          loaders += new FileTemplateLoader(new File(substringAfter(path, "file://")))
        } catch {
          case e: IOException =>
            throw new RuntimeException("templatePath: " + path + " cannot be accessed", e)
        }
      } else if (path.startsWith("webapp://")) {
        loaders += new WebappTemplateLoader(sc, substringAfter(path, "webapp://"))
      } else {
        throw new RuntimeException("templatePath: " + path
          + " is not well-formed. Use [class://|file://|webapp://] seperated with ,")
      }

    }
    new MultiTemplateLoader(loaders.toArray[TemplateLoader])
  }

  /**
   * Load the multi settings from the /META-INF/freemarker.properties and
   * /freemarker.properties file on the classpath
   *
   * @see freemarker.template.Configuration#setSettings for the definition of valid settings
   */

  def loadSetting(): Map[String, String] = {
    val properties = new collection.mutable.HashMap[String, String]
    // 1. first META-INF/freemarker.properties
    for (url <- ClassLoaders.getResources("META-INF/freemarker.properties"))
      properties ++= IOs.readJavaProperties(url)

    // 2. second global freemarker.properties
    for (url <- ClassLoaders.getResources("freemarker.properties"))
      properties ++= IOs.readJavaProperties(url)

    // 3. system properties
    val sysProps = System.getProperties
    val sysKeys = sysProps.propertyNames
    while (sysKeys.hasMoreElements) {
      val key = sysKeys.nextElement.asInstanceOf[String]
      val value: String = sysProps.getProperty(key)
      if (key.startsWith("freemarker.")) {
        properties.put(substringAfter(key, "freemarker."), value)
      }
    }
    properties.toMap

  }

}