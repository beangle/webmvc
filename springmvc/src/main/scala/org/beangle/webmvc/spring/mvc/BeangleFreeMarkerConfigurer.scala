package org.beangle.webmvc.spring.mvc

import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig
import org.beangle.data.model.annotation.config
import org.beangle.webmvc.view.freemarker.Configurations
import javax.servlet.ServletContext
import freemarker.ext.jsp.TaglibFactory
import freemarker.template.ObjectWrapper
import freemarker.template.TemplateException
import freemarker.cache.TemplateLoader
import freemarker.template.Configuration
import org.beangle.webmvc.view.freemarker.BeangleObjectWrapper
import org.beangle.commons.logging.Logging
import freemarker.template.TemplateExceptionHandler
import org.springframework.web.context.ServletContextAware

class BeangleFreeMarkerConfigurer extends FreeMarkerConfig with Logging with ServletContextAware {
  val config = new Configuration()

  disableFreemarkerLogger()

  protected def disableFreemarkerLogger(): Unit = {
    try {
      freemarker.log.Logger.selectLoggerLibrary(freemarker.log.Logger.LIBRARY_NONE)
    } catch {
      case t: Exception => t.printStackTrace()
    }
  }

  override def getConfiguration(): Configuration = {
    config
  }

  override def getTaglibFactory(): TaglibFactory = {
    null
  }

  def setServletContext(servletContext: ServletContext) {
    config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
    config.setDefaultEncoding("UTF-8");
    config.setLocalizedLookup(false);
    config.setWhitespaceStripping(true);

    val properties = Configurations.loadSetting()
    for ((key, value) <- properties) {
      if (null != key && null != value) config.setSetting(key, value)
    }
    info(s"Freemarker properties:$properties")
    config.setObjectWrapper(new BeangleObjectWrapper(true))
    config.setTemplateLoader(Configurations.createTemplateLoader(servletContext, servletContext.getInitParameter("templatePath")))
  }
}