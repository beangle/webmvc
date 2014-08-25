package org.beangle.webmvc.view.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.webmvc.view.freemarker.{ BeangleObjectWrapper, Configurations }

import freemarker.template.{ Configuration, TemplateExceptionHandler }

@description("Freemarker配置提供者")
class FreemarkerConfigurer extends Logging with Initializing {
  //must before configuration init
  Configurations.disableFreemarkerLogger()

  val config = new Configuration()

  var contentType: String = _

  override def init(): Unit = {
    config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER)
    config.setDefaultEncoding("UTF-8")
    config.setLocalizedLookup(false)
    config.setWhitespaceStripping(true)

    val properties = Configurations.loadSetting()
    for ((key, value) <- properties) {
      if (null != key && null != value) config.setSetting(key, value)
    }
    info(s"Freemarker properties:$properties")
    val wrapper = new BeangleObjectWrapper(true)
    wrapper.setUseCache(false)
    config.setObjectWrapper(wrapper)
    val servletContext = ServletContextHolder.context
    config.setTemplateLoader(Configurations.createTemplateLoader(servletContext, servletContext.getInitParameter("templatePath")))

    var content_type = config.getCustomAttribute("content_type").asInstanceOf[String]
    if (null == content_type) content_type = "text/html"
    if (!content_type.contains("charset"))
      content_type += "; charset=" + config.getDefaultEncoding
    contentType = content_type
  }
}