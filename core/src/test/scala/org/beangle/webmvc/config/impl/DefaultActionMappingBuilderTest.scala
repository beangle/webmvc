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
      val mappings = builder.build(classOf[ShowcaseAction], profile).toMap
      assert(null != mappings)
      assert(mappings.size == 8)
      assert(None != mappings.get("/showcase/string"))
      assert(None != mappings.get("/showcase/unit"))
      assert(None != mappings.get("/showcase/request"))
      assert(None != mappings.get("/showcase/param"))
      assert(None != mappings.get("/showcase/cookie"))
      assert(None != mappings.get("/showcase/header"))
      assert(None != mappings.get("/showcase/path/{id}"))
      assert(None != mappings.get("/showcase/echofloat/{num}"))
    }

    it("build plur mapping") {
      val pp = plurProfile.mkProfile(null)
      pp.matches(classOf[ShowcaseAction].getName)
      val mappings = builder.build(classOf[ShowcaseAction], pp).toMap
      assert(null != mappings)
      assert(None != mappings.get("/showcase/string"))
    }
  }
}