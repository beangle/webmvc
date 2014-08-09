package org.beangle.webmvc.struts2.interceptor

import org.beangle.webmvc.context.ContextHolder

import com.opensymphony.xwork2.ActionInvocation
import com.opensymphony.xwork2.interceptor.AbstractInterceptor

@SerialVersionUID(8451445989084058881L)
class FlashInterceptor extends AbstractInterceptor {

  @throws(classOf[Exception])
  override def intercept(invocation: ActionInvocation): String = {
    val ctx = invocation.getInvocationContext
    val result = invocation.invoke()
    val flash = ContextHolder.context.flash
    if (null != flash) flash.nextToNow()
    result
  }

}