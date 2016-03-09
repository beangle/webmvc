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
    val session = request.getSession(false)
    if (null != session) {
      val flash = session.getAttribute("flash").asInstanceOf[Flash]
      if (null != flash) flash.nextToNow()
    }
  }

}