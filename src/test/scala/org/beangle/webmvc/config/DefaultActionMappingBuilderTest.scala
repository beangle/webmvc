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

import org.beangle.webmvc.test.ShowcaseAction
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class DefaultActionMappingBuilderTest extends AnyFunSpec with Matchers {
  val builder = new DefaultActionMappingBuilder
  builder.viewScan = false
  val profile = new Profile("test", "org.beangle.webmvc.test")

  val plurProfile = new ProfileConfig("test", "org.beangle.webmvc.test")
  plurProfile.urlStyle = "plur-seo"
  plurProfile.actionSuffix = "Action"

  describe("DefaultActionMappingBuilder") {
    it("build mapping") {
      profile.matches(classOf[ShowcaseAction].getName)
      val mappings = builder.build(new ShowcaseAction(), classOf[ShowcaseAction], profile).mappings
      assert(null != mappings)
      assert(mappings.size == 8)
      assert(mappings.get("string").isDefined)
      assert(mappings.get("request").isDefined)
      assert(mappings.get("param").isDefined)
      assert(mappings.get("cookie").isDefined)
      assert(mappings.get("header").isDefined)
      assert(mappings("path").url.endsWith("/path/{id}"))
      assert(mappings("echofloat").url.endsWith("/echofloat/{num}"))
    }

    it("build plur mapping") {
      val pp = plurProfile.mkProfile(null, null)
      pp.matches(classOf[ShowcaseAction].getName)
      val mappings = builder.build(new ShowcaseAction(), classOf[ShowcaseAction], pp).mappings
      assert(null != mappings)
      assert(mappings.get("string").isDefined)
    }
  }
}
