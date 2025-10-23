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

import org.beangle.commons.lang.Primitives
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.context.{ActionContext, Argument, Params}

class DynaMethodInvoker(val action: AnyRef, val mapping: RouteMapping) extends Invoker {
  private val method = mapping.method
  private val paramTypes = method.getParameterTypes

  override def invoke(): Any = {
    if (0 == paramTypes.length) {
      method.invoke(action)
    } else {
      val values = convert(ActionContext.current, mapping.arguments, paramTypes)
      method.invoke(action, values: _*)
    }
  }

  def convert(context: ActionContext, args: Array[Argument], paramTypes: Array[Class[_]]): Array[Object] = {
    val values = new Array[Object](paramTypes.length)
    Range(0, paramTypes.length) foreach { i =>
      val pt = paramTypes(i)
      var ov = args(i).value(context)
      if (null != ov) {
        //如果值是个数组，却不需要数组，则取第一个
        if (!pt.isArray && ov.getClass.isArray) ov = ov.asInstanceOf[Array[Object]](0)
        //进行类型转换
        val targetType = if pt.isPrimitive then Primitives.wrap(pt) else pt
        if (!targetType.isAssignableFrom(ov.getClass)) {
          Params.converter.convert(ov, targetType) match {
            case None => throw new RuntimeException(s"Cannot convert $ov to ${targetType.getName}")
            case Some(cv) => ov = cv
          }
        }
        values(i) = ov
      }
      if (args(i).required && null == values(i)) throw new RuntimeException(s"Cannot bind ${i} parameter to ${args(i).name}")
    }
    values
  }
}

@description("句柄构建者,使用method反射调用")
class DynaMethodInvokerBuilder extends InvokerBuilder {

  override def build(action: AnyRef, mapping: RouteMapping): Invoker = {
    new DynaMethodInvoker(action, mapping)
  }
}
