/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.config.Profile
import org.beangle.webmvc.execution.testaction.ShowcaseAction
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.webmvc.config.ProfileConfig

@RunWith(classOf[JUnitRunner])
class DefaultActionMappingBuilderTest extends FunSpec with Matchers {
  val builder = new DefaultActionMappingBuilder
  builder.viewScan = false
  val profile = new Profile("test", "org.beangle.webmvc.execution.testaction")

  val plurProfile = new ProfileConfig("test", "org.beangle.webmvc.execution.testaction")
  plurProfile.urlStyle = "plur-seo"
  plurProfile.actionSuffix = "Action"

  describe("DefaultActionMappingBuilder") {
    it("build mapping") {
      profile.matches(classOf[ShowcaseAction].getName)
      val mappings = builder.build(new ShowcaseAction(), classOf[ShowcaseAction], profile).mappings
      assert(null != mappings)
      assert(mappings.size == 7)
      assert(None != mappings.get("string"))
      assert(None != mappings.get("request"))
      assert(None != mappings.get("param"))
      assert(None != mappings.get("cookie"))
      assert(None != mappings.get("header"))
      assert(mappings.get("path").get.name == "path/{id}")
      assert(mappings.get("echofloat").get.name == "echofloat/{num}")
    }

    it("build plur mapping") {
      val pp = plurProfile.mkProfile(null,null)
      pp.matches(classOf[ShowcaseAction].getName)
      val mappings = builder.build(new ShowcaseAction(), classOf[ShowcaseAction], pp).mappings
      assert(null != mappings)
      assert(None != mappings.get("string"))
    }
  }
}