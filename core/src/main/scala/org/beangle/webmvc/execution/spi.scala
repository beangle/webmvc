package org.beangle.webmvc.execution

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.ActionMapping
import java.lang.reflect.Method

@spi
trait InvocationReactor {
  def invoke(handler: Handler, mapping: ActionMapping): Unit
}

@spi
trait HandlerBuilder {
  def build(action: AnyRef, mapping: ActionMapping): Handler
}