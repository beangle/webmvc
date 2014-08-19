package org.beangle.webmvc.view.template

import java.io.Writer
import org.beangle.webmvc.view.component.Component
import org.beangle.commons.lang.annotation.spi

@spi
trait TemplateEngine {

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component): Unit

  def suffix: String

}