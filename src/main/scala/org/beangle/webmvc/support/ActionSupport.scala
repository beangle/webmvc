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

package org.beangle.webmvc.support

import org.beangle.webmvc.execution.ResultException
import org.beangle.webmvc.view.RawView

abstract class ActionSupport extends RouteSupport, ParamSupport {
  protected def raw(data: Any): RawView = {
    RawView(data)
  }

  protected def error[T](result: Any): T = {
    throw new ResultException(500, result)
  }

  protected def error[T](code: Int, result: Any): T = {
    throw new ResultException(code, result)
  }

}
