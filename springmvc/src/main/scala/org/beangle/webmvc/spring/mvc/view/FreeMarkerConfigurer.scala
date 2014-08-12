package org.beangle.webmvc.spring.mvc.view

import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.freemarker.{ BeangleObjectWrapper, Configurations }
import org.springframework.web.context.ServletContextAware

import freemarker.ext.jsp.TaglibFactory
import freemarker.template.{ Configuration, TemplateExceptionHandler }
import javax.servlet.ServletContext

class FreeMarkerConfigurer extends org.springframework.web.servlet.view.freemarker.FreeMarkerConfig with Logging with ServletContextAware {
  //must before configuration init
  Configurations.disableFreemarkerLogger()
  val config = new Configuration()

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