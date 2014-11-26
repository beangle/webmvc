package org.beangle.webmvc.view.tag

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.annotation.spi
import java.io.Writer

@spi
trait TemplateEngine {

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component): Unit

  def suffix: String

}