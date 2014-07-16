package org.beangle.webmvc.struts2.dispatcher

import java.{ util => ju }

import org.apache.struts2.ServletActionContext
import org.apache.struts2.dispatcher.multipart.MultiPartRequestWrapper
import org.apache.struts2.dispatcher.ng.{ ExecuteOperations, InitOperations, PrepareOperations }
import org.apache.struts2.dispatcher.ng.servlet.ServletHostConfig

import com.opensymphony.xwork2.ActionContext

import javax.servlet.{ ServletConfig, ServletException }
import javax.servlet.http.{ HttpServlet, HttpServletRequest, HttpServletResponse }

@SerialVersionUID(-2962412407855583998L)
class ActionServlet extends HttpServlet {
  private var prepare: PrepareOperations = _
  private var execute: ExecuteOperations = _

  @throws(classOf[ServletException])
  override def init(filterConfig: ServletConfig) = {
    val init = new InitOperations()
    try {
      val config = new ServletHostConfig(filterConfig)
      init.initLogging(config)
      ActionContext.setContext(new ActionContext(new ju.HashMap[String, Object]()))
      ServletActionContext.setServletContext(config.getServletContext())
      val dispatcher = init.initDispatcher(config)
      init.initStaticContentLoader(config, dispatcher)
      ActionContext.setContext(null)
      prepare = new PrepareOperations(filterConfig.getServletContext(), dispatcher)
      execute = new ExecuteOperations(filterConfig.getServletContext(), dispatcher)
    } finally {
      init.cleanup()
    }
  }

  @throws(classOf[Exception])
  override protected def service(request: HttpServletRequest, response: HttpServletResponse) = {
    var newRequest: HttpServletRequest = null
    try {
      prepare.createActionContext(request, response)
      prepare.assignDispatcherToThread()
      // prepare.setEncodingAndLocale(request, response)
      val oldRequest = request
      newRequest = prepare.wrapRequest(request)
      // Dislike Struts2 optimization for jstl .It delegate request.getAttribute to ongl valuestack.
      // When use freemarker,it was not nessesary.
      if (!(newRequest.isInstanceOf[MultiPartRequestWrapper])) newRequest = oldRequest
      val mapping = prepare.findActionMapping(newRequest, response)
      if (mapping == null) {
        response.setStatus(HttpServletResponse.SC_NOT_FOUND)
      } else {
        execute.executeAction(newRequest, response, mapping)
      }
    } finally {
      prepare.cleanupRequest(newRequest)
    }
  }

  override def destroy() {
    prepare.cleanupDispatcher()
  }
}