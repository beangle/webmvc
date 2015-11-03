/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.config.impl

import java.lang.annotation.Annotation
import java.lang.reflect.Method
import scala.Range
import org.beangle.commons.http.HttpMethods.GET
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.Strings.{ isNotEmpty, split }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.commons.lang.reflect.Reflections.{ getAnnotation, isAnnotationPresent }
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.api.annotation.{ DefaultNone, action, cookie, header, ignore, mapping, param, response, view, views }
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, RouteMapping, ActionMappingBuilder, Profile, Path }
import org.beangle.webmvc.context.Argument
import org.beangle.webmvc.context.impl.{ CookieArgument, HeaderArgument, ParamArgument, RequestArgument, ResponseArgument }
import org.beangle.webmvc.view.{ TemplateResolver, ViewBuilder }
import org.beangle.webmvc.view.impl.ViewManager

@description("缺省的ActionMapping构建器")
class DefaultActionMappingBuilder extends ActionMappingBuilder with Logging {

  var viewBuilder: ViewBuilder = _

  var viewScan = true

  var viewManager: ViewManager = _

  override def build(bean: AnyRef, clazz: Class[_], profile: Profile): ActionMapping = {
    val nameAndspace = ActionNameBuilder.build(clazz, profile)
    val actionName = nameAndspace._1
    val views = buildViews(clazz, profile)
    val config = new ActionMapping(bean, clazz, actionName, nameAndspace._2, views, profile)
    val mappings = new collection.mutable.HashMap[String, RouteMapping]
    val classInfo = ClassInfo.get(clazz)
    classInfo.methods foreach {
      case (methodName, minfos) =>
        val mappingMehtods = new collection.mutable.HashSet[Method]
        minfos.filter(m => isActionMethod(m.method, classInfo)) foreach { methodinfo =>
          val method = methodinfo.method
          val annTuple = getAnnotation(method, classOf[mapping])
          val ann = if (null == annTuple) null else annTuple._1
          val httpMethod = if (null != ann && isNotEmpty(ann.method)) ann.method.toUpperCase.intern else GET
          val name = if (null != ann) (if (ann.value.startsWith("/")) ann.value.substring(1) else ann.value) else methodName
          val url = if (name == "") actionName else (actionName + "/" + name)
          val urlParams = Path.parse(url)
          val urlParamIdx = urlParams.map(e => (e._2, e._1))
          val urlPathNames = urlParamIdx.keySet.toList.sorted.map { i => urlParamIdx(i) }

          val annotationsList = if (null == annTuple) method.getParameterAnnotations else annTuple._2.getParameterAnnotations

          val parameterTypes = method.getParameterTypes()
          val arguments = Range(0, annotationsList.length) map { i =>
            var argument: Argument = null
            var j = 0
            val annotations = annotationsList(i)
            while (j < annotations.length && null == argument) {
              argument = annotations(j) match {
                case p: param  => new ParamArgument(p.value, p.required || parameterTypes(i).isPrimitive, p.defaultValue)
                case c: cookie => new CookieArgument(c.value, c.required || parameterTypes(i).isPrimitive, c.defaultValue)
                case h: header => new HeaderArgument(h.value, h.required || parameterTypes(i).isPrimitive, h.defaultValue)
                case _         => null
              }
              j += 1
            }
            if (argument == null) {
              argument = if (parameterTypes(i).getName == "javax.servlet.http.HttpServletRequest") RequestArgument
              else if (parameterTypes(i).getName == "javax.servlet.http.HttpServletResponse") ResponseArgument
              else {
                if (i < urlPathNames.length) new ParamArgument(urlPathNames(i), true, DefaultNone.value)
                else null
              }
            }
            argument
          }
          if (arguments.size == 0 || !arguments.exists(a => a == null)) {
            mappingMehtods += method
            if (mappingMehtods.size == 1) {
              var defaultView = defaultViewName(method)
              if (null != defaultView && defaultView.contains(",") && !views.isEmpty) {
                defaultView = Strings.split(defaultView, ",") find (v => views.contains(v)) match {
                  case Some(v) => v
                  case _       => defaultView
                }
              }
              val mapping = new RouteMapping(httpMethod, config, method, name, arguments.toArray, urlParams, defaultView)
              mappings.put(method.getName, mapping)
            } else {
              logger.warn(s"Only support one method, but $mappingMehtods finded")
            }
          } else {
            //ignore arguments contain  all null
            if (arguments.exists(a => a != null)) {
              throw new RuntimeException(s"Cannot find enough param for $method,Using @mapping or @param")
            }
          }
        }
    }
    config.mappings = mappings.toMap
    config
  }

  /**
   * whether a action method is entry
   * <li> Cannot contain $
   * <li> Cannot starts with get/set/is
   * <li> Cannot annotated with @ignore
   * <li> Cannot be a field get accessor
   * <li> Without @response/@mapping and return  type is not [String/View]
   */
  private def isActionMethod(method: Method, classInfo: ClassInfo): Boolean = {
    val methodName = method.getName

    if (methodName.contains("$")) return false
    if (methodName == "get" || methodName.startsWith("get") && methodName.length > 3 && Character.isUpperCase(methodName.charAt(3))
      || methodName.startsWith("is") && methodName.length > 2 && Character.isUpperCase(methodName.charAt(2))) {
      return false
    }

    //filter ignore
    if (null != getAnnotation(method, classOf[ignore])) return false

    val returnType = method.getReturnType()
    if (null == getAnnotation(method, classOf[response]) || null == getAnnotation(method, classOf[mapping])) {
      //filter method don't return string or view
      if (returnType != classOf[String] && returnType != classOf[View]) return false
    } else {
      if (returnType == classOf[Unit]) throw new RuntimeException(s"${method} return type is unit ")
    }
    //filter field
    if (method.getParameterTypes.length == 0 && !classInfo.getMethods(methodName + "_$eq").isEmpty) return false
    true
  }

  protected def buildViews(clazz: Class[_], profile: Profile): Map[String, View] = {
    if (!viewScan) return Map.empty
    val resolver = viewManager.getResolver(profile.viewType).orNull
    if (null == resolver) return Map.empty

    val viewMap = new collection.mutable.HashMap[String, View]
    // load annotation results
    var results = new Array[view](0)
    var rs = clazz.getAnnotation(classOf[views])
    if (null == rs) {
      val an = clazz.getAnnotation(classOf[action])
      if (null != an) results = an.views()
    } else {
      results = rs.value()
    }
    val annotationResults = new collection.mutable.HashSet[String]
    if (null != results) {
      for (result <- results) {
        viewMap.put(result.name, viewBuilder.build(result, profile.viewType))
        annotationResults += result.name
      }
    }
    // load ftl convension results
    val suffix = profile.viewSuffix
    if (suffix.endsWith(".ftl")) {
      ClassInfo.get(clazz).getMethods foreach { mi =>
        val viewName = defaultViewName(mi.method)
        if (null != viewName && !annotationResults.contains(viewName)) {
          Strings.split(viewName, ",") foreach { v =>
            val view = resolver.resolve(clazz, v, suffix)
            if (null != view) viewMap.put(v, view)
          }
        }
      }
    }
    viewMap.toMap
  }

  protected def defaultViewName(m: Method): String = {
    if (classOf[String].equals(m.getReturnType) && !isAnnotationPresent(m, classOf[ignore]) && !isAnnotationPresent(m, classOf[response])) {
      val mappingAnn = getAnnotation(m, classOf[mapping])
      if (m.getParameterTypes.length == 0 || null != mappingAnn || containParamAnnotation(m.getParameterAnnotations)) {
        val name = m.getName.toLowerCase
        if (name.startsWith("get") || Strings.contains(name, "$")) {
          null
        } else {
          if (null != mappingAnn && Strings.isNotEmpty(mappingAnn._1.view)) mappingAnn._1.view
          else DefaultActionMappingBuilder.defaultView(m.getName, m.getName)
        }
      } else null
    } else {
      null
    }
  }

  private def containParamAnnotation(annotations: Array[Array[Annotation]]): Boolean = {
    var i = 0
    while (i < annotations.length) {
      var j = 0
      while (j < annotations(i).length) {
        if (annotations(i)(j).isInstanceOf[param]) return true;
        j += 1
      }
      i += 1
    }
    false
  }
}

object DefaultActionMappingBuilder {
  private val methodViews = Map(("search", "list"), ("query", "list"), ("edit", "form"), ("home", "index"), ("execute", "index"), ("add", "new"))

  def defaultView(methodName: String, viewName: String): String = {
    if (null == viewName) methodViews.getOrElse(methodName, methodName)
    else methodViews.getOrElse(viewName, viewName)
  }
}