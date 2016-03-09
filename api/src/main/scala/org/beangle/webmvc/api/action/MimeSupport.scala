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
package org.beangle.webmvc.api.action

import java.net.URL

import org.beangle.commons.http.accept.ContentNegotiationManager
import org.beangle.commons.lang.{Chars, ClassLoaders, Strings}
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.util.{CookieUtils, RequestUtils}
import org.beangle.webmvc.api.annotation.ignore
import org.beangle.webmvc.api.context.{ActionMessages, ActionContext, Flash}
import org.beangle.webmvc.api.view.{ForwardActionView, RedirectActionView, View}

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}

trait MimeSupport {

  var contentNegotiationManager: ContentNegotiationManager = _

  def isRequestCsv: Boolean = {
    if (null == contentNegotiationManager) false
    else {
      contentNegotiationManager.resolve(ActionContext.current.request).exists { p => p.getBaseType == "text/csv" }
    }
  }
}