package org.beangle.webmvc.config.action

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.view.freemarker.FreemarkerConfigurer
import org.beangle.commons.lang.ClassLoaders

class FreemarkerAction extends ActionSupport {
  var freemarkerConfigurer: FreemarkerConfigurer = _

  def index(): String = {
    put("config", freemarkerConfigurer.config)
    put("properties", freemarkerConfigurer.properties)
    put("templatePath", freemarkerConfigurer.templatePath)
    val configLocations = ClassLoaders.getResources("META-INF/freemarker.properties") ++ ClassLoaders.getResources("freemarker.properties")
    put("configLocations", configLocations)
    forward()
  }
}