package org.beangle.webmvc.view.freemarker

import java.io.{ File, IOException }

import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{ split, substringAfter }
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.context.ServletContextHolder

import freemarker.cache.{ FileTemplateLoader, MultiTemplateLoader, TemplateLoader, WebappTemplateLoader }
import freemarker.template.{ Configuration, TemplateExceptionHandler }
import javax.servlet.ServletContext

@description("Freemarker配置提供者")
class FreemarkerConfigurer extends Logging with Initializing {
  //must before configuration init
  disableFreemarkerLogger()

  val config = new Configuration()

  var contentType: String = _

  override def init(): Unit = {
    config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER)
    config.setDefaultEncoding("UTF-8")
    config.setLocalizedLookup(false)
    config.setWhitespaceStripping(true)

    for ((key, value) <- properties) {
      if (null != key && null != value) config.setSetting(key, value)
    }

    info(s"Freemarker properties:$properties")
    val wrapper = new BeangleObjectWrapper(true)
    wrapper.setUseCache(false)
    config.setObjectWrapper(wrapper)
    config.setTemplateLoader(createTemplateLoader(ServletContextHolder.context, templatePath))

    var content_type = config.getCustomAttribute("content_type").asInstanceOf[String]
    if (null == content_type) content_type = "text/html"
    if (!content_type.contains("charset"))
      content_type += "; charset=" + config.getDefaultEncoding
    contentType = content_type
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
  def properties: Map[String, String] = {
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

  def disableFreemarkerLogger(): Unit = {
    try {
      freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE)
    } catch {
      case t: Exception => t.printStackTrace()
    }
  }

  def templatePath: String = {
    ServletContextHolder.context.getInitParameter("templatePath")
  }

}