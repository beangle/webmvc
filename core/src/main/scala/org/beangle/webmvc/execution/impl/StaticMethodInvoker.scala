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

package org.beangle.webmvc.execution.impl

import java.lang.reflect.Method
import javassist._
import javassist.compiler.Javac
import jakarta.servlet.http.{HttpServletRequest, HttpServletResponse}
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.{ClassLoaders, Primitives}
import org.beangle.commons.logging.Logging
import org.beangle.web.action.annotation.DefaultNone
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.context.Argument
import org.beangle.webmvc.execution.invoker.Placeholder
import org.beangle.webmvc.execution.{Invoker, InvokerBuilder}

@description("句柄构建者，生成静态调用类")
class StaticMethodInvokerBuilder extends InvokerBuilder with Logging {

  var handlerCount = 0

  def build(action: AnyRef, mapping: RouteMapping): Invoker = {
    val method = mapping.method
    val actionClassName = action.getClass.getName
    val invokerName = action.getClass.getSimpleName + "_" + method.getName + "_" + handlerCount
    val invokerClassName = "org.beangle.webmvc.execution.invoker." + invokerName

    val body = new CodeGenerator().gen(method, mapping, action)
    val pool = new ClassPool(true)
    pool.appendClassPath(new LoaderClassPath(ClassLoaders.defaultClassLoader))
    val cct = pool.makeClass(invokerClassName)
    cct.addInterface(pool.get(classOf[Invoker].getName))
    val javac = new Javac(cct)

    cct.addField(javac.compile("private final " + actionClassName + " action;").asInstanceOf[CtField])
    cct.addMethod(javac.compile("public Object action() {return action;}").asInstanceOf[CtMethod])

    val ctor = javac.compile("public " + invokerName + "(" + actionClassName + " action){}").asInstanceOf[CtConstructor]
    ctor.setBody("this.action=$1;")
    cct.addConstructor(ctor)
    val handleMethod = javac.compile("public Object invoke() {return null;}").asInstanceOf[CtMethod]
    handleMethod.setBody(body)
    cct.addMethod(handleMethod)

    //    cct.debugWriteFile("/tmp/invokers")
    val maked = cct.toClass(classOf[Placeholder])
    cct.detach()
    handlerCount += 1
    maked.getConstructor(action.getClass).newInstance(action).asInstanceOf[Invoker]
  }
}

class CodeGenerator {
  def gen(method: Method, mapping: RouteMapping, action: AnyRef): String = {
    val nonevoid = method.getReturnType != classOf[Unit]
    if (method.getParameterTypes.length == 0) {
      if (nonevoid) {
        if (method.getReturnType.isPrimitive) {
          s"{return ${Primitives.wrap(method.getReturnType).getName}.valueOf(action.${method.getName}());}\n"
        } else {
          s"{return action.${method.getName}();}\n"
        }
      } else {
        s"{action.${method.getName}();return null;}\n"
      }
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
      sb ++= "org.beangle.web.action.context.ActionContext context = org.beangle.web.action.context.ActionContext$.MODULE$.current();\n"
      if (needConverter)
        sb ++= "org.beangle.commons.collection.MapConverter converter = org.beangle.web.action.context.Params$.MODULE$.converter();\n"
      if (needRequest)
        sb ++= "jakarta.servlet.http.HttpServletRequest request = context.request();\n"
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
              sb ++= s"String v$pt_index = org.beangle.web.servlet.util.CookieUtils.getCookieValue(request,$q${argument.name}$q);\n"
              sb ++= s"if(null==v$pt_index){" + handleNone(argument, pt_index, action.getClass, method) + "}\n"
            case "org.beangle.webmvc.context.impl.HeaderArgument" =>
              paramAsString = true
              sb ++= s"String v$pt_index = request.getHeader($q${argument.name}$q);\n"
              sb ++= s"if(null==v$pt_index){" + handleNone(argument, pt_index, action.getClass, method) + "}\n"
          }
          paramList += (if (paramAsString && pt == classOf[String]) s"v$pt_index" else s"vp$pt_index")
          if (!pt.isPrimitive) {
            if (!(paramAsString && pt == classOf[String])) {
              sb ++= s"${pt.getName} vp$pt_index=null;\n"
              sb ++= s"scala.Option tmp =  converter.convert(v$pt_index, ${pt.getName}.class);\n"
              sb ++= s"if(!tmp.isEmpty()) vp$pt_index = (${pt.getName})tmp.get();\n"
              if (argument.required) {
                sb ++= s"if(null == vp$pt_index) throw new IllegalArgumentException(${q}Cannot bind parameter $argument for ${action.getClass.getName}.${method.getName}$q);\n"
              }
            }
          } else {
            sb ++= s"Object vWrapper$pt_index = null;\n"
            sb ++= s"scala.Option tmp =  converter.convert(v$pt_index, ${Primitives.wrap(pt).getName}.class);\n"
            sb ++= s"if(!tmp.isEmpty()) vWrapper$pt_index =tmp.get();\n"
            sb ++= s"if(null== vWrapper$pt_index) throw new IllegalArgumentException(${q}Cannot bind parameter $argument for ${action.getClass.getName}.${method.getName}$q);\n"
            sb ++= s"${pt.getName} vp$pt_index = ((${Primitives.wrap(pt).getName})vWrapper$pt_index).${pt.getName}Value();"
          }
        }
        pt_index += 1
      }

      if (nonevoid) {
        if (method.getReturnType.isPrimitive) {
          sb ++= (s"return ${Primitives.wrap(method.getReturnType).getName}.valueOf(action.${method.getName}(" + paramList.mkString(",") + "));\n")
        } else {
          sb ++= (s"return action.${method.getName}(" + paramList.mkString(",") + ");\n")
        }
      } else {
        sb ++= (s"action.${method.getName}(" + paramList.mkString(",") + ");\nreturn null;\n")
      }
      sb ++= "}"
      sb.toString
    }
  }

  def handleNone(argument: Argument, idx: Int, actionClass: Class[_], method: Method): String = {
    val q = "\""
    if (argument.required) {
      if (argument.defaultValue == DefaultNone.value) {
        s"throw new IllegalArgumentException(${q}Cannot bind parameter $argument for ${actionClass.getName}.${method.getName}$q);"
      } else {
        s"v$idx=$q${argument.defaultValue}$q;"
      }
    } else {
      if (argument.defaultValue != DefaultNone.value) s"v$idx=$q${argument.defaultValue}$q;" else ""
    }
  }
}
