package org.beangle.webmvc.api.context

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class ParamsTest extends FunSpec with Matchers {

  describe("Params") {
    it("getInt will ignore empty string") {
      val params = Map[String, Any]("y" -> "", "z" -> 1)

      val context = new ActionContext(null, null, null, params)
      ContextHolder.contexts.set(context)

      assert(None == Params.getInt("x"))
      assert(None == Params.getInt("y"))
      assert(Some(1) == Params.getInt("z"))
    }
  }
}