package org.beangle.webmvc.execution

import org.beangle.commons.lang.primitive.MutableInt
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.config.ActionMapping

trait Handler {
  def action: AnyRef
  def handle(mapping: ActionMapping): Any
}