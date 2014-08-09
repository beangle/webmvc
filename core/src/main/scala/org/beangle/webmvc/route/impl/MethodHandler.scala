package org.beangle.webmvc.route.impl

import java.lang.reflect.Method
import org.beangle.commons.lang.Primitives
import org.beangle.webmvc.annotation.param
import org.beangle.webmvc.helper.Params
import org.beangle.webmvc.route.Handler
import org.beangle.webmvc.context.ContextHolder

case class MethodHandler(action: AnyRef, method: Method) extends Handler {

  override def handle(params: Map[String, Any]): Any = {
    val paramTypes = method.getParameterTypes
    if (0 == paramTypes.length) {
      method.invoke(action)
    } else {
      val annotationsList = method.getParameterAnnotations()
      val values = new collection.mutable.ListBuffer[Object]
      var i = 0;
      for (annotations <- annotationsList) {
        for (annotation <- annotations) {
          annotation match {
            case p: param => {
              val pt = paramTypes(i)
              val ov = params.get(p.value).getOrElse(ContextHolder.context.params.get(p.value).orNull)
              val pValue = if (null != ov && !pt.isArray() && ov.getClass.isArray()) ov.asInstanceOf[Array[_]](0) else ov
              if (Primitives.isWrapperType(pt)) {
                values += Params.converter.convert(pValue, pt).asInstanceOf[Object]
              } else {
                val value = Params.converter.convert(pValue, Primitives.wrap(pt)).asInstanceOf[Object]
                if (null == value) {
                  //FIXME binding error , migrate from other framework process this situation.
                } else {
                  values += value
                }
              }
            }
            case _ =>
          }
        }
        i += 1
      }
      if (values.size != paramTypes.length) {
        throw new IllegalArgumentException(s"Cannot  bind parameter to ${method.getName} in action ${action.getClass}")
      } else {
        method.invoke(action, values.toArray: _*)
      }
    }
  }
}