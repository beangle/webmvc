package org.beangle.webmvc.struts2.view

import java.io.{File, IOException}
import java.util.{Enumeration, Properties}

import org.apache.struts2.views.freemarker.FreemarkerManager
import org.apache.struts2.views.freemarker.FreemarkerManager.INITPARAM_CONTENT_TYPE
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.ClassLoaders
import org.beangle.commons.lang.Strings.{split, substringAfter}
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.freemarker.{BeangleClassTemplateLoader, BeangleObjectWrapper}

import freemarker.cache.{FileTemplateLoader, MultiTemplateLoader, TemplateLoader, WebappTemplateLoader}
import freemarker.template.{ObjectWrapper, TemplateException}
import javax.servlet.ServletContext

class BeangleFreemarkerManager extends FreemarkerManager with Logging {

  disableFreemarkerLogger()

  protected def disableFreemarkerLogger(): Unit = {
    try {
      freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE)
    } catch {
      case t: Exception => t.printStackTrace()
    }
  }

  override protected def createObjectWrapper(servletContext: ServletContext): ObjectWrapper = {
    val wrapper = new BeangleObjectWrapper(altMapWrapper)
    // cacheBeanWrapper should be false in most case.
    wrapper.setUseCache(cacheBeanWrapper)
    wrapper
  }

  /**
   * disable configuration using theme temploader
   * because only tag based on theme ,common pages don't based on theme
   */
  override protected def configureTemplateLoader(templateLoader: TemplateLoader): Unit = {
    config.setTemplateLoader(templateLoader)
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
  override protected def createTemplateLoader(servletContext: ServletContext, templatePath: String): TemplateLoader = {
    // construct a FileTemplateLoader for the init-param 'TemplatePath'
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
        loaders += new WebappTemplateLoader(servletContext, substringAfter(path, "webapp://"))
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
   * @see freemarker.template.Configuration#setSettings for the definition of
   *      valid settings
   */
  override protected def loadSettings(servletContext: ServletContext): Unit = {
    try {
      val properties = new collection.mutable.HashMap[String, String]
      // 1. first META-INF/freemarker.properties
      for (url <- ClassLoaders.getResources("META-INF/freemarker.properties", getClass))
        properties ++= IOs.readJavaProperties(url)

      // 2. second global freemarker.properties
      for (url <- ClassLoaders.getResources("freemarker.properties", getClass()))
        properties ++= IOs.readJavaProperties(url)

      // 3. system properties
      val sysProps: Properties = System.getProperties()
      val sysKeys: Enumeration[_] = sysProps.propertyNames()
      while (sysKeys.hasMoreElements()) {
        val key = sysKeys.nextElement().asInstanceOf[String]
        val value: String = sysProps.getProperty(key)
        if (key.startsWith("freemarker.")) {
          properties.put(substringAfter(key, "freemarker."), value)
        }
      }

      // 4 add setting and log info
      val sb: StringBuilder = new StringBuilder()
      val keys = new collection.mutable.ListBuffer[String]
      keys ++= properties.keySet
      keys.sorted
      for (key <- keys) {
        val value = properties(key)
        if (null != key && null != value) {
          addSetting(key, value)
          if (sb.size > 0) sb.append(",")
          sb.append(key).append("->").append(value)
        }
      }
      info(s"Freemarker properties:${sb} ")
    } catch {
      case e: TemplateException =>
        error("Error while setting freemarker.properties", e)
    }
  }

  @throws(classOf[TemplateException])
  override def addSetting(name: String, value: String): Unit = {
    if (name.equals("content_type") || name.equals(INITPARAM_CONTENT_TYPE)) {
      contentType = value
      config.setCustomAttribute("content_type", value)
    } else {
      super.addSetting(name, value)
    }
  }
}