/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.tag

import java.io.StringWriter
import java.{ util => ju }

import org.beangle.commons.collection.page.Page
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.Handler

import _root_.freemarker.template.utility.StringUtil
import javax.servlet.http.HttpServletRequest

class CoreModels(context: ComponentContext, request: HttpServletRequest) extends AbstractModels(context, request) {

  val textProvider = ActionContext.current.textProvider.get

  def url(url: String) = {
    context.uriRender.render(Handler.mapping, url)
  }

  def now = new ju.Date

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

  def text(name: String): String = {
    textProvider(name, name)
  }

  def text(name: String, arg0: Object): String = {
    textProvider(name, name, arg0)
  }

  def text(name: String, arg0: Object, arg1: Object): String = {
    textProvider(name, name, arg0, arg1)
  }

}