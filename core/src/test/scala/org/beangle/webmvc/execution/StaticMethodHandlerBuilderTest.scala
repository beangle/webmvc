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
package org.beangle.webmvc.execution

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.execution.impl.StaticMethodInvokerBuilder
import org.beangle.webmvc.execution.testaction.ShowcaseAction
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.scalatest.junit.JUnitRunner
import org.beangle.webmvc.config.impl.DefaultActionMappingBuilder
import org.beangle.webmvc.config.Profile

@RunWith(classOf[JUnitRunner])
class StaticMethodHandlerBuilderTest extends FunSpec with Matchers {
  val builder = new StaticMethodInvokerBuilder()
  val mappingBuilder = new DefaultActionMappingBuilder
  mappingBuilder.viewScan = false
  val profile = new Profile("test", "org.beangle.webmvc.execution.testaction")
  profile.matches(classOf[ShowcaseAction].getName)

  describe("StaticMethodHandlerBuilder") {
    it("build handler") {
      val action = new ShowcaseAction
      val mappings = mappingBuilder.build(action,classOf[ShowcaseAction], profile).mappings
      builder.build(action, mappings("string")).invoke()
      builder.build(action, mappings("request"))
      builder.build(action, mappings("param"))
      builder.build(action, mappings("cookie"))
      builder.build(action, mappings("header"))
      builder.build(action, mappings("path"))
      builder.build(action, mappings("echofloat"))
    }
  }
}