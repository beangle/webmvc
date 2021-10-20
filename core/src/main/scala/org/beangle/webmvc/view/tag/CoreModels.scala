/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.view.tag

import jakarta.servlet.http.HttpServletRequest
import org.beangle.commons.collection.page.Page
import org.beangle.web.action.context.ActionContext
import org.beangle.webmvc.execution.MappingHandler
import org.beangle.template.api.{AbstractModels, ComponentContext}
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.commons.text.escape.JavascriptEscaper

import java.io.StringWriter
import java.util as ju

class CoreModels(context: ComponentContext, request: HttpServletRequest) extends AbstractModels(context) {

  def url(url: String): String = {
    val mapping = ActionContext.current.handler.asInstanceOf[MappingHandler].mapping
    this.context.services("uriRender").asInstanceOf[ActionUriRender].render(mapping, url)
  }

  def base: String = {
    request.getContextPath
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
        sw.write(JavascriptEscaper.escape(value,false))
        if (em.hasMoreElements()) sw.write('&')
      }
    }
    sw.toString()
  }

  def isPage(data: Object) = data.isInstanceOf[Page[_]]

  def text(name: String): String = {
    context.textProvider(name, name)
  }

  def text(name: String, arg0: Object): String = {
    context.textProvider(name, name, arg0)
  }

  def text(name: String, arg0: Object, arg1: Object): String = {
    context.textProvider(name, name, arg0, arg1)
  }

}
