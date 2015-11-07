/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.webmvc.dispatch.impl

import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.web.url.UrlRender
import org.beangle.webmvc.api.action.To
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.dispatch.{ ActionUriRender, RequestMapper }
import org.beangle.webmvc.config.Configurer

@description("根据uri相对地址反向生成绝对地址")
class DefaultActionUriRender extends ActionUriRender {

  val render = new UrlRender

  var configurer: Configurer = _

  override def render(initmapping: RouteMapping, uri: String): String = {
    val context = ActionContext.current
    val contextPath = context.request.getServletContext().getContextPath
    if (uri.charAt(0) == '/') return contextPath + uri

    var params: collection.mutable.Map[String, String] = null
    val action = initmapping.action
    val mapping =
      if (uri.charAt(0) == '!') {
        var dotIdx = uriEndIndexOf(uri)
        params = To(uri).parameters
        action.mappings(uri.substring(1, dotIdx))
      } else {
        val namespace = action.namespace
        var backStep = 0
        var index = 0
        while (uri.startsWith("../", index)) {
          index += 3
          backStep += 1
        }
        val goNamespace =
          if (backStep > 0) {
            var chars = new Array[Char](namespace.length)
            namespace.getChars(0, chars.length, chars, 0)
            var findedSlash = chars.length - 1
            while (backStep > 0 && findedSlash > -1) {
              if (chars(findedSlash) == '/') backStep -= 1
              findedSlash -= 1
            }
            namespace.substring(0, findedSlash + 2)
          } else {
            namespace + "/"
          }
        val finalURL = goNamespace + (if (index == 0) uri else uri.substring(index))
        val struts = To(finalURL).toStruts
        params = struts.parameters

        val actionName = new StringBuilder
        actionName.append(struts.namespace).append('/').append(struts.name)
        configurer.getActionMapping(actionName.toString) match {
          case Some(cfg) => cfg.mappings(if (null == struts.method) cfg.profile.defaultMethod else struts.method)
          case None => throw new RuntimeException(s"Cannot find $actionName mapping")
        }
      }
    val tourl = mapping.toURL(params, context.params)
    params --= mapping.urlParams.keys
    contextPath + tourl.params(params).url
  }

  /**
   * Return uri end index of url
   */
  def uriEndIndexOf(url: String): Int = {
    var lastIndex = url.length
    var chars = new Array[Char](lastIndex)
    url.getChars(0, lastIndex, chars, 0)
    var i = 0
    while (i < lastIndex) {
      var c = chars(i)
      if (c == '.' || c == '?') return i
      i += 1
    }
    lastIndex
  }
}