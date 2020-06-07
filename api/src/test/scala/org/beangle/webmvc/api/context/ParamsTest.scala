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
package org.beangle.webmvc.api.context

import javax.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.junit.runner.RunWith
import org.mockito.Mockito.mock
import org.scalatest.matchers.should.Matchers
import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParamsTest extends AnyFunSpec with Matchers {

  describe("Params") {
    it("getInt will ignore empty string") {
      val params = Map[String, Any]("y" -> "", "z" -> 1)

      val request = mock(classOf[HttpServletRequest])
      val response = mock(classOf[HttpServletResponse])

      val context = new ActionContext(request, response, null, params)
      ActionContext.set(context)

      assert(Params.getInt("x").isEmpty)
      assert(Params.getInt("y").isEmpty)
      assert(Params.getInt("z").contains(1))
    }
  }
}
