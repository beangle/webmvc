package org.beangle.webmvc.execution.interceptors

import org.beangle.commons.lang.annotation.description
import org.beangle.commons.web.intercept.OncePerRequestInterceptor
import org.beangle.webmvc.api.context.Flash

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

/**
 * Process Flash in context
 */
@description("请求之间传递参数的flash拦截器")
class FlashInterceptor extends OncePerRequestInterceptor {

  override def doPostInvoke(request: HttpServletRequest, response: HttpServletResponse): Unit = {
    val session = request.getSession()
    if (null != session) {
      val flash = session.getAttribute("flash").asInstanceOf[Flash]
      if (null != flash) flash.nextToNow()
    }
  }

}