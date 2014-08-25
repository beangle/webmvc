package org.beangle.webmvc.view.template

import java.io.Writer
import org.beangle.webmvc.view.component.Component
import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.Profile

@spi
trait TemplateEngine {

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component): Unit

  def suffix: String

}

@spi
trait TemplatePathMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}

@spi
trait TemplateResolver {
  def resolve(actionClass: Class[_], viewName: String, suffix: String): String
}