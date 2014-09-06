package org.beangle.webmvc.dispatch

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.ActionMapping

import javax.servlet.http.HttpServletRequest

@spi
trait RequestMapper {

  def resolve(request: HttpServletRequest): Option[RequestMapping]

  def resolve(uri: String): Option[RequestMapping]
}

/**
 * Url render
 */
trait ActionUriRender {
  def render(action: ActionMapping, uri: String): String
}