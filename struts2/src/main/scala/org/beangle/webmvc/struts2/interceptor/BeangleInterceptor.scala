package org.beangle.webmvc.struts2.interceptor

import org.apache.struts2.StrutsStatics.{ HTTP_REQUEST, HTTP_RESPONSE }
import org.beangle.webmvc.context.{ ActionContext, ContextHolder, Flash }

import com.opensymphony.xwork2.ActionInvocation
import com.opensymphony.xwork2.interceptor.AbstractInterceptor
import com.opensymphony.xwork2.{ ActionContext => XworkContext }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

//FIXME missing file upload feature
@SerialVersionUID(8451445989084058881L)
class BeangleInterceptor extends AbstractInterceptor {

  @throws(classOf[Exception])
  override def intercept(invocation: ActionInvocation): String = {
    val ctx = invocation.getInvocationContext
    val request = ctx.get(HTTP_REQUEST).asInstanceOf[HttpServletRequest]
    val response = ctx.get(HTTP_RESPONSE).asInstanceOf[HttpServletResponse]
    val context = new ActionContext(request, response, params)
    ContextHolder.contexts.set(context)
    var result = invocation.invoke()
    val flash = context.flash
    if (null != flash) flash.nextToNow()
    return result
  }

  private def params: Map[String, Any] = {
    val context = ContextHolder.context
    val itor = XworkContext.getContext().getParameters().entrySet().iterator()
    val paramsBuilder = new collection.mutable.HashMap[String, Any]
    while (itor.hasNext) {
      val entry = itor.next()
      var value:Any = entry.getValue
      if (value.getClass.isArray) {
        val arrayValue = value.asInstanceOf[Array[Any]]
        if (arrayValue.length == 1) value = arrayValue(0)
      }
      paramsBuilder.put(entry.getKey(), value)
    }
    paramsBuilder.toMap
  }
}