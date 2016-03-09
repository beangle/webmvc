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
