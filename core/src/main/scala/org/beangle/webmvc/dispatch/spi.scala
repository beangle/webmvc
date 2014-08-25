package org.beangle.webmvc.dispatch

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.ActionMapping
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.config.ActionConfig

@spi
trait RequestMapper {

  def resolve(request: HttpServletRequest): Option[RequestMapping]

  def resolve(uri: String): Option[RequestMapping]

  def antiResolve(className: String, method: String): Option[ActionMapping]

  def antiResolve(actionName: String): Option[ActionConfig]
}

/**
 * Url render
 */
trait ActionUriRender {
  def render(action: ActionMapping, uri: String): String
}