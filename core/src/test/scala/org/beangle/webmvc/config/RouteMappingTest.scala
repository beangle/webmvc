/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.config

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.http.HttpMethods
import org.beangle.webmvc.context.Argument
import org.beangle.webmvc.api.view.View

@RunWith(classOf[JUnitRunner])
class RouteMappingTest extends FunSpec with Matchers {

  describe("RouteMapping") {
    it("url") {
      val action = new ActionMapping(null, null, "/", "/", Map.empty[String, View], null)
      val rm = new RouteMapping(HttpMethods.GET, action, null, "{path*}", Array.empty[Argument], Map.empty[String, Integer], null)
      assert("/{path*}" == rm.url)
    }
  }
}
