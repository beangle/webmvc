package org.beangle.webmvc.execution.impl

import java.lang.reflect.Method

import scala.Range

import org.beangle.commons.lang.Primitives
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.api.context.{ ContextHolder, Params }
import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.execution.{ Handler, HandlerBuilder }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class MethodHandler(val action: AnyRef, val method: Method) extends Handler {
  val paramTypes = method.getParameterTypes

  override def handle(mapping: ActionMapping): Any = {
    if (0 == paramTypes.length) {
      method.invoke(action)
    } else {
      val values = new Array[Object](paramTypes.length)
      var binded = 0
      val context = ContextHolder.context
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
            values(i) = Params.converter.convert(pValue, pt).asInstanceOf[Object]
          } else {
            values(i) = Params.converter.convert(pValue, Primitives.wrap(pt)).asInstanceOf[Object]
          }
        }
      }
      method.invoke(action, values: _*)
    }
  }
}

@description("句柄构建者,使用method反射调用")
class MethodHandlerBuilder extends HandlerBuilder {

  override def build(action: AnyRef, method: Method): Handler = {
    new MethodHandler(action, method)
  }
}