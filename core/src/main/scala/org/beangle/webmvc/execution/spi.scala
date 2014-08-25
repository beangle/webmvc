package org.beangle.webmvc.execution

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.ActionMapping

@spi
trait InvocationReactor {
  def invoke(handler: Handler, mapping: ActionMapping): Unit
}