/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.execution.impl

import java.lang.reflect.Method

import scala.Range

import org.beangle.commons.lang.Primitives
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.api.context.{ ActionContextHolder, Params }
import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.execution.{ Handler, HandlerBuilder }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class DynaMethodHandler(val action: AnyRef, val method: Method) extends Handler {
  val paramTypes = method.getParameterTypes

  override def handle(mapping: ActionMapping): Any = {
    if (0 == paramTypes.length) {
      method.invoke(action)
    } else {
      val values = new Array[Object](paramTypes.length)
      var binded = 0
      val context = ActionContextHolder.context
      val params = context.params
      val arguments = mapping.arguments
      Range(0, paramTypes.length) foreach { i =>
        val pt = paramTypes(i)
        if (pt == classOf[HttpServletRequest]) values(i) = context.request
        else if (pt == classOf[HttpServletResponse]) values(i) = context.response
        else {
          val ov = arguments(i).value(context)
          val pValue = if (null != ov && !pt.isArray() && ov.getClass.isArray()) ov.asInstanceOf[Array[_]](0) else ov
          if (Primitives.isWrapperType(pt)) {
            Params.converter.convert(pValue, pt) foreach { v => values(i) = v.asInstanceOf[Object] }
          } else {
            if (pt.isPrimitive) {
              Params.converter.convert(pValue, Primitives.wrap(pt)) foreach { v => values(i) = v.asInstanceOf[Object] }
            } else {
              Params.converter.convert(pValue, pt) foreach { v => values(i) = v.asInstanceOf[Object] }
            }
          }
          if (arguments(i).required && null == values(i)) throw new RuntimeException(s"Cannot convert $pValue to ${pt.getName}")
        }
      }
      method.invoke(action, values: _*)
    }
  }
}

@description("句柄构建者,使用method反射调用")
class DynaMethodHandlerBuilder extends HandlerBuilder {

  override def build(action: AnyRef, mapping: ActionMapping): Handler = {
    new DynaMethodHandler(action, mapping.method)
  }
}