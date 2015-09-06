package org.beangle.webmvc.view.tag

import java.io.StringWriter
import java.{ util => ju }

import org.beangle.commons.collection.page.Page
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.view.UITheme

import _root_.freemarker.template.utility.StringUtil
import javax.servlet.http.HttpServletRequest

class CoreModels(context: ComponentContext, request: HttpServletRequest) extends AbstractModels(context, request) {

  val textResource = ContextHolder.context.textResource

  def url(url: String) = {
    context.uriRender.render(ActionContextHelper.getMapping(ContextHolder.context).action, url)
  }

  def now = new ju.Date

  val uitheme = {
    val base = request.getContextPath
    val location = if (base.length() < 2) "/static/themes/"
    else base + "/static/themes/"
    new UITheme(location)
  }

  /**
   * query string and form control
   */
  def paramstring: String = {
    val sw = new StringWriter()
    val em = request.getParameterNames()
    while (em.hasMoreElements()) {
      val attr = em.nextElement()
      val value = request.getParameter(attr)
      if (!attr.equals("x-requested-with")) {
        sw.write(attr)
        sw.write('=')
        sw.write(StringUtil.javaScriptStringEnc(value))
        if (em.hasMoreElements()) sw.write('&')
      }
    }
    return sw.toString()
  }

  def isPage(data: Object) = data.isInstanceOf[Page[_]]

  def text(name: String) = textResource(name, name)

  def text(name: String, arg0: Object) {
    textResource(name, name, arg0)
  }

  def text(name: String, arg0: Object, arg1: Object) {
    textResource(name, name, arg0, arg1)
  }

}