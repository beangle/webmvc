package org.beangle.webmvc.execution

import java.io.StringWriter
import java.{ util => ju }
import org.beangle.webmvc.execution.testaction.ShowcaseAction
import org.beangle.webmvc.view.freemarker.BeangleClassTemplateLoader
import org.junit.runner.RunWith
import org.scalatest.{ FunSpec, Matchers }
import freemarker.template.Configuration
import javassist.ClassPool
import javassist.compiler.Javac
import org.scalatest.junit.JUnitRunner
import javassist.CtField
import javassist.CtConstructor
import javassist.CtMethod
import java.lang.reflect.Method
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.lang.Primitives
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.execution.impl.StaticHandlerBuilder

@RunWith(classOf[JUnitRunner])
class StaticHandlerProxyTest extends FunSpec with Matchers {
  val builder= new StaticHandlerBuilder()
  describe("StaticHandlerProxy") {
    it("gen") {
      val action = new ShowcaseAction
      val handler =builder.build(action, action.getClass.getMethod("index"))
      handler.handle(null)
      builder.build(action, action.getClass.getMethod("index2")).handle(null)
      builder.build(action, action.getClass.getMethod("index3", classOf[HttpServletRequest], classOf[HttpServletResponse]))
      builder.build(action, action.getClass.getMethod("index4", classOf[HttpServletRequest], classOf[Long]))
    }
  }
}