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
package org.beangle.webmvc.config.impl

import org.junit.runner.RunWith
import org.scalatest.Matchers
import org.scalatest.junit.JUnitRunner
import org.scalatest.FunSpec
import org.beangle.webmvc.execution.testaction.IndexAction
import org.beangle.webmvc.config.Profile

/**
 * @author chaostone
 */
@RunWith(classOf[JUnitRunner])
class ActionNameBuilderTest extends FunSpec with Matchers {

  describe("ActionNameBuilder") {
    it("build indexmapping") {
      val profile = new Profile("test", "org.beangle.webmvc.execution.testaction")
      profile.matches(classOf[IndexAction].getName)
      val result = ActionNameBuilder.build(classOf[IndexAction], profile)
      assert(result._1 =="/")
      assert(result._2 =="/")
    }
  }
}