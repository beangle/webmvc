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

package org.beangle.webmvc.execution

import org.beangle.webmvc.config.{DefaultActionMappingBuilder, Profile}
import org.beangle.webmvc.test.ShowcaseAction
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class StaticMethodHandlerBuilderTest extends AnyFunSpec with Matchers {
  val builder = new StaticMethodInvokerBuilder()
  val mappingBuilder = new DefaultActionMappingBuilder
  mappingBuilder.viewScan = false
  val profile = new Profile("test", "org.beangle.webmvc.test")
  profile.matches(classOf[ShowcaseAction].getName)

  describe("StaticMethodHandlerBuilder") {
    it("build handler") {
      val action = new ShowcaseAction
      val mappings = mappingBuilder.build(action, classOf[ShowcaseAction], profile).mappings
      builder.build(action, mappings("string")).invoke()
      builder.build(action, mappings("request"))
      builder.build(action, mappings("param"))
      builder.build(action, mappings("cookie"))
      builder.build(action, mappings("header"))
      builder.build(action, mappings("path"))
      builder.build(action, mappings("echofloat"))
      builder.build(action, mappings("ok"))
    }
  }
}
