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