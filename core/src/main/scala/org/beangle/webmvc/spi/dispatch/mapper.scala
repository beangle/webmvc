package org.beangle.webmvc.spi.dispatch

import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.config.ActionMapping

trait Handler {
  def handle(mapping: ActionMapping): Any
}

class RequestMapping(val action: ActionMapping, val handler: Handler, val params: collection.Map[String, Any])

trait RequestMapper {

  def resolve(request: HttpServletRequest): Option[RequestMapping]

  def antiResolve(clazz: Class[_], method: String): Option[RequestMapping]

  def resolve(uri: String): Option[RequestMapping]

}