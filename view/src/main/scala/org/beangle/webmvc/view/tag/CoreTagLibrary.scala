/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.impl.IndexableIdGenerator
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.view.TagLibrary

/**
 * Beangle tag Library
 *
 * @author chaostone
 * @since 2.0
 */
@description("beangle webmvc core 标签库")
class CoreTagLibrary extends AbstractTagLibrary {

  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef = {
    new CoreModels(buildComponentContext(req), req)
  }

}
