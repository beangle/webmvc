package org.beangle.webmvc.dispatch

import java.lang.reflect.Method

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.Profile

import javax.servlet.http.HttpServletRequest

@spi
trait ActionMappingBuilder {

  def build(clazz: Class[_], profile: Profile): Seq[Tuple2[ActionMapping, Method]]
}

@spi
trait RequestMapper {

  def resolve(request: HttpServletRequest): Option[RequestMapping]

  def antiResolve(clazz: Class[_], method: String): Option[RequestMapping]

  def resolve(uri: String): Option[RequestMapping]

}