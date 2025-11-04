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

import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.webmvc.config.{DefaultActionMappingBuilder, Profile}
import org.beangle.webmvc.context.ActionContext
import org.beangle.webmvc.test.ShowcaseAction
import org.beangle.webmvc.view.{PathView, Status}
import org.mockito.Mockito.{mock, when}
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

class MethodInvokerTest extends AnyFunSpec, Matchers {

  val mappingBuilder = new DefaultActionMappingBuilder
  mappingBuilder.viewScan = false
  val profile = new Profile("org.beangle.webmvc.test")
  profile.matches(classOf[ShowcaseAction].getName)
  val mappings = mappingBuilder.build(new ShowcaseAction(), classOf[ShowcaseAction], profile).mappings
  val params: Map[String, Any] = Map("id" -> 12345L)
  val request = mock(classOf[HttpServletRequest])
  when(request.getHeader("Accept")).thenReturn("text/html")

  val response = mock(classOf[HttpServletResponse])

  val ctx = new ActionContext(request, response, null, params)
  ActionContext.set(ctx)

  describe("MethodInvoker") {
    val action = new ShowcaseAction
    val builder = new DynaMethodInvokerBuilder
    it("invoke method") {
      var invoker = builder.build(action, mappings("string"))
      assert(invoker.invoke() == PathView(null))

      invoker = builder.build(action, mappings("param"))
      assert(invoker.invoke() == Status.Ok)

      invoker = builder.build(action, mappings("request"))
      assert(invoker.invoke() == Status.Ok)

      invoker = builder.build(action, mappings("path"))
      assert(invoker.invoke() == Status.Ok)

      invoker = builder.build(action, mappings("echoid"))
      assert(invoker.invoke() == "in Showcase echo id:12345")

      invoker = builder.build(action, mappings("ok"))
      assert(invoker.invoke() == true)
    }
  }
}
