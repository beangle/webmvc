package org.beangle.webmvc.execution.impl

import java.lang.reflect.Method
import scala.Range
import org.beangle.commons.lang.Primitives
import org.beangle.webmvc.api.context.{ ContextHolder, Params }
import org.beangle.webmvc.dispatch.ActionMapping
import org.beangle.webmvc.execution.Handler
import org.beangle.webmvc.execution.Interceptor

class MethodHandler(val action: AnyRef, val method: Method) extends Handler {
  val paramTypes = method.getParameterTypes

  override def handle(mapping: ActionMapping): Any = {
    if (0 == paramTypes.length) {
      if (!preHandle(mapping.interceptors)) return null
      val result = method.invoke(action)
      postHandle(mapping.interceptors, result)
      result
    } else {
      if (!preHandle(mapping.interceptors)) return null
      
      val values = new Array[Object](paramTypes.length)
      var binded = 0
      val params = ContextHolder.context.params
      val paramNames = mapping.paramNames
      Range(0, paramTypes.length) foreach { i =>
        val paramName = paramNames(i)
        val pt = paramTypes(i)
        val ov = params.get(paramName).orNull
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
      
      if (binded == paramTypes.length) {
        val result = method.invoke(action, values: _*)
        postHandle(mapping.interceptors, result)
        result
      } else throw new IllegalArgumentException(s"Cannot  bind parameter to ${method.getName} in action ${action.getClass}")
    }
  }
  def preHandle(interceptors: Array[Interceptor]): Boolean = {
    var i = 0
    while (i < interceptors.length) {
      val interceptor = interceptors(i)
      if (!interceptor.preHandle(this)) return false
      i += 1
    }
    true
  }

  def postHandle(interceptors: Array[Interceptor], result: Any): Unit = {
    var i = interceptors.length - 1
    while (i >= 0) {
      val interceptor = interceptors(i)
      interceptor.postHandle(this, result)
      i -= 1
    }
  }
}