package org.beangle.webmvc.execution.impl

import java.io.StringWriter
import java.{ util => ju }
import org.beangle.commons.lang.{ ClassLoaders, Primitives }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.api.annotation.DefaultNone
import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.execution.{ Handler, HandlerBuilder }
import javassist.{ ClassPool, CtConstructor, CtField, CtMethod, LoaderClassPath }
import javassist.compiler.Javac
import java.lang.reflect.Method
import org.beangle.webmvc.context.Argument
import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

@description("句柄构建者，生成静态调用类")
class StaticMethodHandlerBuilder extends HandlerBuilder with Logging {

  var handlerCount = 0

  def build(action: AnyRef, mapping: ActionMapping): Handler = {
    val method = mapping.method
    val actionClassName = action.getClass().getName()
    val hanlderName = action.getClass().getSimpleName() + "_" + method.getName() + "_" + handlerCount
    val hanlderClassName = "org.beangle.webmvc.execution.handlers." + hanlderName

    val body = new CodeGenerator().gen(method, mapping, action)
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
    try{
    handleMethod.setBody(body)
    }catch{
      case t:Throwable => println(body)
    }
    cct.addMethod(handleMethod)

    //cct.debugWriteFile("/tmp/handlers")
    val maked = cct.toClass()
    cct.detach()
    handlerCount += 1
    maked.getConstructor(action.getClass).newInstance(action).asInstanceOf[Handler]
  }
}

class CodeGenerator {
  def gen(method: Method, mapping: ActionMapping, action: AnyRef): String = {
    val nonevoid = method.getReturnType != classOf[Unit]
    if (method.getParameterTypes.length == 0) {
      if (nonevoid) s"{return action.${method.getName}();}\n"
      else s"{action.${method.getName}();return null;}\n"
    } else {
      val q = "\""
      var needRequest = false
      var needParam = false
      var needConverter = false
      val parameterTypes = method.getParameterTypes
      var argu_index = 0
      for (argu <- mapping.arguments) {
        val arguClassName = argu.getClass.getName
        if (arguClassName == "org.beangle.webmvc.context.impl.HeaderArgument" || arguClassName == "org.beangle.webmvc.context.impl.CookieArgument") {
          needRequest = true
          if (parameterTypes(argu_index) != classOf[String]) needConverter = true
        }
        if (arguClassName == "org.beangle.webmvc.context.impl.ParamArgument") {
          needParam = true
          needConverter = true
        }
        argu_index += 1
      }
      val sb = new StringBuilder("{\n")
      sb ++= "org.beangle.webmvc.api.context.ActionContext context = org.beangle.webmvc.api.context.ContextHolder$.MODULE$.context();\n"
      if (needConverter)
        sb ++= "org.beangle.commons.collection.MapConverter converter = org.beangle.webmvc.api.context.Params$.MODULE$.converter();\n"
      if (needRequest)
        sb ++= "javax.servlet.http.HttpServletRequest request = context.request();\n"
      if (needParam)
        sb ++= "scala.collection.immutable.Map params = context.params();\n"

      val paramList = new collection.mutable.ListBuffer[String]
      var pt_index = 0
      while (pt_index < parameterTypes.length) {
        val pt = parameterTypes(pt_index)
        if (pt == classOf[HttpServletRequest]) {
          paramList += (if (needRequest) "request" else "context.request()")
        } else if (pt == classOf[HttpServletResponse]) {
          paramList += "context.response()"
        } else {
          val argument = mapping.arguments(pt_index)
          var paramAsString = false
          argument.getClass.getName match {
            case "org.beangle.webmvc.context.impl.ParamArgument" =>
              sb ++= s"scala.Option vp$pt_index = params.get($q${argument.name}$q);\n"
              sb ++= s"Object v$pt_index = null;\n"
              sb ++= (s"if(vp$pt_index.isEmpty()){\n" + handleNone(argument, pt_index, action.getClass, method))
              sb ++= "\n}else{\n"
              sb ++= s"v$pt_index =vp$pt_index.get();\n"
              if (!pt.isArray)
                sb ++= s"if(v$pt_index.getClass().isArray()) v$pt_index= ((Object[])v$pt_index)[0];\n"
              sb ++= "}\n"
            case "org.beangle.webmvc.context.impl.CookieArgument" =>
              paramAsString = true
              sb ++= s"String v$pt_index = org.beangle.commons.web.util.CookieUtils.getCookieValue(request,$q${argument.name}$q);\n"
              sb ++= s"if(null==v$pt_index){" + handleNone(argument, pt_index, action.getClass, method) + "}\n"
            case "org.beangle.webmvc.context.impl.HeaderArgument" =>
              paramAsString = true
              sb ++= s"String v$pt_index = request.getHeader($q${argument.name}$q);\n"
              sb ++= s"if(null==v$pt_index){" + handleNone(argument, pt_index, action.getClass, method) + "}\n"
          }
          paramList += (if (paramAsString && pt == classOf[String]) s"v$pt_index" else s"vp$pt_index")
          if (!pt.isPrimitive) {
            if (!(paramAsString && pt == classOf[String])) {
              sb ++= s"${pt.getName} vp$pt_index = (${pt.getName})converter.convert(v$pt_index, ${pt.getName}.class);\n"
              if (argument.required) {
                sb ++= s"if(null == vp$pt_index) throw new IllegalArgumentException(${q}Cannot bind parameter ${argument} for ${action.getClass.getName}.${method.getName}$q);\n"
              }
            }
          } else {
            sb ++= s"Object vWrapper$pt_index = converter.convert(v$pt_index, ${Primitives.wrap(pt).getName}.class);"
            sb ++= s"if(null== vWrapper$pt_index) throw new IllegalArgumentException(${q}Cannot bind parameter ${argument} for ${action.getClass.getName}.${method.getName}$q);\n"
            sb ++= s"${pt.getName} vp$pt_index = ((${Primitives.wrap(pt).getName})vWrapper$pt_index).${pt.getName}Value();"
          }
        }
        pt_index += 1
      }

      if (nonevoid) {
        sb ++= (s"return action.${method.getName}(" + paramList.mkString(",") + ");\n")
      } else {
        sb ++= (s"action.${method.getName}(" + paramList.mkString(",") + ");\nreturn null;\n")
      }
      sb ++= ("}")
      sb.toString
    }
  }
  def handleNone(argument: Argument, idx: Int, actionClass: Class[_], method: Method): String = {
    val q = "\""
    if (argument.required) {
      if (argument.defaultValue == DefaultNone.value) {
        s"throw new IllegalArgumentException(${q}Cannot bind parameter ${argument} for ${actionClass.getName}.${method.getName}$q);"
      } else {
        s"v$idx=$q${argument.defaultValue}$q"
      }
    } else {
      if (argument.defaultValue != DefaultNone.value) {
        s"v$idx=$q${argument.defaultValue}$q"
      } else {
        ""
      }
    }
  }
}