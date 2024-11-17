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

package org.beangle.webmvc.config

import org.beangle.commons.net.http.HttpMethods
import org.beangle.webmvc.context.Argument
import org.beangle.webmvc.view.View
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class RouteMappingTest extends AnyFunSpec with Matchers {

  describe("RouteMapping") {
    it("url") {
      val action = new ActionMapping(null, null, "/", "/", Map.empty[String, View], null)
      val rm = RouteMapping(HttpMethods.GET, action, null, "{path*}", Array.empty[Argument], Map.empty[String, Integer], null)
      assert("/{path*}" == rm.url)
    }
  }
}
