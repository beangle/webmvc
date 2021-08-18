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

import org.beangle.commons.lang.annotation.description
import org.beangle.template.api.{AbstractTagLibrary, IndexableIdGenerator}
import org.beangle.web.action.context.ActionContext
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.i18n.NullTextProvider

/**
 * Beangle tag Library
 *
 * @author chaostone
 * @since 2.0
 */
@description("beangle webui 标签库")
class BeangleTagLibrary extends AbstractTagLibrary {

  var uriRender: ActionUriRender = _

  override def models(): AnyRef = {
    val req = ActionContext.current.request
    new BeangleModels(this.buildComponentContext(), req)
  }

  def buildServices(): Map[String, AnyRef] = {
    val req = ActionContext.current.request
    val queryString = req.getQueryString
    val fullpath = if (null == queryString) req.getRequestURI else req.getRequestURI + queryString
    val idGenerator = new IndexableIdGenerator(String.valueOf(Math.abs(fullpath.hashCode)))
    val textProvider = ActionContext.current.textProvider.getOrElse(NullTextProvider)
    Map("idGenerator" -> idGenerator, "textProvider" -> textProvider, "uriRender" -> uriRender)
  }
}
