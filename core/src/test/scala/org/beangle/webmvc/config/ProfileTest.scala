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
package org.beangle.webmvc.config

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ProfileTest extends FunSpec with Matchers {

  describe("Profile") {
    it("compare") {
      var child = new Profile("parent", "org.beangle.aa.bb.web.action")
      var parent = new Profile("parent", "org.beangle.*.web.action")
      assert(parent.compareTo(child) > 0)

      child = new Profile("parent", "org.beangle.bb.web.action")
      parent = new Profile("parent", "org.beangle.*.web.action")
      assert(parent.compareTo(child) > 0)
    }
  }
}