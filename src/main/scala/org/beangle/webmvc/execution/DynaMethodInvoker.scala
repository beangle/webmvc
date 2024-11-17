/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.execution

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.lang.Primitives
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.context.{ActionContext, Params}

class DynaMethodInvoker(val action: AnyRef, val mapping: RouteMapping) extends Invoker {
  private val method = mapping.method
  private val paramTypes = method.getParameterTypes

  override def invoke(): Any = {
    if (0 == paramTypes.length) {
      method.invoke(action)
    } else {
      val values = new Array[Object](paramTypes.length)
      val context = ActionContext.current
      val arguments = mapping.arguments
      Range(0, paramTypes.length) foreach { i =>
        val pt = paramTypes(i)
        if (pt == classOf[HttpServletRequest]) values(i) = context.request
        else if (pt == classOf[HttpServletResponse]) values(i) = context.response
        else {
          val ov = arguments(i).value(context)
          val pValue = if (null != ov && !pt.isArray && ov.getClass.isArray) ov.asInstanceOf[Array[_]](0) else ov
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
class DynaMethodInvokerBuilder extends InvokerBuilder {

  override def build(action: AnyRef, mapping: RouteMapping): Invoker = {
    new DynaMethodInvoker(action, mapping)
  }
}
