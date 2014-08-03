package org.beangle.webmvc.struts2.view

import org.apache.struts2.views.freemarker.FreemarkerManager
import org.apache.struts2.views.freemarker.FreemarkerManager.INITPARAM_CONTENT_TYPE
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.freemarker.{ BeangleObjectWrapper, Configurations }

import freemarker.cache.TemplateLoader
import freemarker.template.{ ObjectWrapper, TemplateException }
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

  override protected def createTemplateLoader(servletContext: ServletContext, templatePath: String): TemplateLoader = {
    Configurations.createTemplateLoader(servletContext, templatePath)
  }

  override protected def loadSettings(servletContext: ServletContext): Unit = {
    val properties = Configurations.loadSetting()
    for ((key, value) <- properties){
      if (null != key && null != value) addSetting(key, value)
    }
    info(s"Freemarker properties:$properties")
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