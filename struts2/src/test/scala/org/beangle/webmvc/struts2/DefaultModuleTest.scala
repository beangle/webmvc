package org.beangle.webmvc.struts2

import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import org.scalatest.junit.JUnitRunner
import org.beangle.commons.lang.Strings

@RunWith(classOf[JUnitRunner])
class DefaultModuleTest extends FunSpec with Matchers {

  describe("DefaultModule") {
    it("provider properties") {
      val properties = new DefaultModule().properties
      assert(null != properties)
      assert(properties.contains("struts.url.http.port"))
      assert(properties("struts.objectFactory") == "beangle")
    }
  }
}