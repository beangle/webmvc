package org.beangle.webmvc.route.impl

import java.lang.reflect.Method
import org.beangle.commons.lang.Primitives
import org.beangle.webmvc.annotation.param
import org.beangle.webmvc.helper.Params
import org.beangle.webmvc.route.Handler
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.route.ActionMapping

class MethodHandler(val action: AnyRef, val method: Method) extends Handler {
  val paramTypes = method.getParameterTypes

  override def handle(mapping: ActionMapping, params: Map[String, Any]): Any = {
    if (0 == paramTypes.length) {
      method.invoke(action)
    } else {
      val values = new Array[Object](paramTypes.length)
      var binded = 0
      val paramNames = mapping.paramNames
      Range(0, paramTypes.length) foreach { i =>
        val paramName = paramNames(i)
        val pt = paramTypes(i)
        val ov = params.get(paramName).getOrElse(ContextHolder.context.params.get(paramName).orNull)
        val pValue = if (null != ov && !pt.isArray() && ov.getClass.isArray()) ov.asInstanceOf[Array[_]](0) else ov
        if (Primitives.isWrapperType(pt)) {
          values(i) = Params.converter.convert(pValue, pt).asInstanceOf[Object]
          binded += 1
        } else {
          val value = Params.converter.convert(pValue, Primitives.wrap(pt)).asInstanceOf[Object]
          if (null == value) {
            //FIXME binding error , migrate from other framework process this situation.
          } else {
            values(i) = value
            binded += 1
          }
        }
      }
      if (binded == paramTypes.length) method.invoke(action, values: _*)
      else throw new IllegalArgumentException(s"Cannot  bind parameter to ${method.getName} in action ${action.getClass}")
    }
  }
}