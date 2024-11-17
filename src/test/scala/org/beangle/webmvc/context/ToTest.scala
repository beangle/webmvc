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

package org.beangle.webmvc.context

import org.beangle.webmvc.To
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class ToTest extends AnyFunSpec with Matchers {

  describe("ToBuilder") {
    it("correct analysis uri") {
      var touri = To("/a/b/c.html?p1=1&p2=2", null)
      assert(touri.suffix == ".html")
      assert(touri.uri == "/a/b/c")
      assert(touri.parameters.size == 2)

      touri = To("/a/b.c/d.xml?p1=1", null)
      assert(touri.suffix == ".xml")
      assert(touri.uri == "/a/b.c/d")
      assert(touri.parameters.size == 1)

      touri = To("!info?orderBy=user.code", null)
      assert(touri.suffix == null)
      assert(touri.uri == "!info")
      assert(touri.parameters.size == 1)

      touri = To("a.b/c?p1=1", null)
      assert(touri.suffix == null)
      assert(touri.uri == "a.b/c")
      assert(touri.parameters.size == 1)
    }
  }

}
