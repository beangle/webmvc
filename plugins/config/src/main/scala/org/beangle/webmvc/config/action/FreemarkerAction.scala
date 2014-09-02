package org.beangle.webmvc.config.action

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.view.freemarker.FreemarkerConfigurer

class FreemarkerAction extends ActionSupport {
  var freemarkerConfigurer: FreemarkerConfigurer = _

  def index(): String = {
    put("config", freemarkerConfigurer.config)
    put("properties", freemarkerConfigurer.properties)
    put("templatePath",freemarkerConfigurer.templatePath)
    forward()
  }
}