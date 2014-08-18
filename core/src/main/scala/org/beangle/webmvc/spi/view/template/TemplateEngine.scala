package org.beangle.webmvc.spi.view.template

import java.io.Writer
import org.beangle.webmvc.view.component.Component

trait TemplateEngine {

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component): Unit

  def suffix: String

}