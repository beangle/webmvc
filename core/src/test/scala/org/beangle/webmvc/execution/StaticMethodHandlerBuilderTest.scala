package org.beangle.webmvc.execution

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.execution.impl.StaticMethodHandlerBuilder
import org.beangle.webmvc.execution.testaction.ShowcaseAction
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.scalatest.junit.JUnitRunner
import org.beangle.webmvc.config.impl.DefaultActionMappingBuilder
import org.beangle.webmvc.config.Profile

@RunWith(classOf[JUnitRunner])
class StaticMethodHandlerBuilderTest extends FunSpec with Matchers {
  val builder = new StaticMethodHandlerBuilder()
  val mappingBuilder = new DefaultActionMappingBuilder
  mappingBuilder.viewScan = false
  val profile = new Profile("test", "org.beangle.webmvc.execution.testaction")
  profile.matches(classOf[ShowcaseAction].getName)

  describe("StaticMethodHandlerBuilder") {
    it("build handler") {
      val mappings = mappingBuilder.build(classOf[ShowcaseAction], profile).toMap
      val action = new ShowcaseAction
      builder.build(action, mappings("/showcase/string")).handle(null)
      builder.build(action, mappings("/showcase/unit"))
      builder.build(action, mappings("/showcase/request"))
      builder.build(action, mappings("/showcase/param"))
      builder.build(action, mappings("/showcase/cookie"))
      builder.build(action, mappings("/showcase/header"))
      builder.build(action, mappings("/showcase/path/{id}"))
      builder.build(action, mappings("/showcase/echofloat/{num}"))
    }
  }
}