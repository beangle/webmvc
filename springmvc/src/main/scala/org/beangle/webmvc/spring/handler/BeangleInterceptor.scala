package org.beangle.webmvc.spring.handler

import org.springframework.web.servlet.HandlerInterceptor
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.context.ActionContext
import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.springframework.web.servlet.ModelAndView

/**
 * Create actionContext
 * FIXME Upload
 */
class BeangleInterceptor extends HandlerInterceptor {

  override def preHandle(request: HttpServletRequest, response: HttpServletResponse, handler: Object): Boolean = {
    val context = new ActionContext(request, response, getParams(request))
    ContextHolder.contexts.set(context)
    true
  }

  override def postHandle(req: HttpServletRequest, res: HttpServletResponse, handler: Object, mv: ModelAndView): Unit = {
  }
  override def afterCompletion(req: HttpServletRequest, res: HttpServletResponse, handler: Object, ex: Exception): Unit = {
  }

  private def getParams(request: HttpServletRequest): Map[String, Any] = {
    val context = ContextHolder.context
    val itor = request.getParameterMap.entrySet.iterator
    val paramsBuilder = new collection.mutable.HashMap[String, Any]
    while (itor.hasNext) {
      val entry = itor.next()
      paramsBuilder.put(entry.getKey, entry.getValue)
    }
    //    request match {
    //      case mp: MultiPartRequestWrapper => paramsBuilder ++= getUploads(mp)
    //      case _ =>
    //    }
    paramsBuilder.toMap
  }
}