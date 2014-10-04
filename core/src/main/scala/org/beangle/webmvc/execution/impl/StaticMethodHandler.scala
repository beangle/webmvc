package org.beangle.webmvc.execution.impl

import java.io.StringWriter
import java.{ util => ju }

import org.beangle.commons.lang.{ ClassLoaders, Primitives }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.api.annotation.DefaultNone
import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.execution.{ Handler, HandlerBuilder }
import org.beangle.webmvc.view.freemarker.BeangleClassTemplateLoader

import freemarker.template.Configuration
import javassist.{ ClassPool, CtConstructor, CtField, CtMethod, LoaderClassPath }
import javassist.compiler.Javac

@description("句柄构建者，生成静态调用类")
class StaticMethodHandlerBuilder extends HandlerBuilder with Logging {
  val config = new Configuration()
  config.setTemplateLoader(new BeangleClassTemplateLoader())
  config.setTagSyntax(2)
  config.setEncoding(config.getLocale(), "UTF-8")
  val template = config.getTemplate("/static_handler_handle.ftl")

  var handlerCount = 0

  def build(action: AnyRef, mapping: ActionMapping): Handler = {
    val method = mapping.method
    val data = new ju.HashMap[String, Object]
    val actionClassName = action.getClass().getName()
    val hanlderName = action.getClass().getSimpleName() + "_" + method.getName() + "_" + handlerCount
    val hanlderClassName = "org.beangle.webmvc.execution.handlers." + hanlderName
    data.put("method", method)
    data.put("mapping", mapping)
    data.put("actionClass", action.getClass)
    data.put("Primitives", Primitives)
    data.put("DefaultNone", DefaultNone.value)
    val sw = new StringWriter
    template.process(data, sw)
    //info(sw.toString)
    val pool = new ClassPool(true)
    pool.appendClassPath(new LoaderClassPath(ClassLoaders.defaultClassLoader))
    val cct = pool.makeClass(hanlderClassName)
    cct.addInterface(pool.get(classOf[Handler].getName))
    val javac = new Javac(cct)

    cct.addField(javac.compile("private final " + actionClassName + " action;").asInstanceOf[CtField])
    cct.addMethod(javac.compile("public Object action() {return action;}").asInstanceOf[CtMethod])

    val ctor = javac.compile("public " + hanlderName + "(" + actionClassName + " action){}").asInstanceOf[CtConstructor]
    ctor.setBody("this.action=$1;")
    cct.addConstructor(ctor)
    val handleMethod = javac.compile("public Object handle(org.beangle.webmvc.config.ActionMapping mapping) {return null;}").asInstanceOf[CtMethod]
    handleMethod.setBody(sw.toString)
    cct.addMethod(handleMethod)

    //cct.debugWriteFile("/tmp/handlers")
    val maked = cct.toClass()
    cct.detach()
    handlerCount += 1
    maked.getConstructor(action.getClass).newInstance(action).asInstanceOf[Handler]
  }
}