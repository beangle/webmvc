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
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.annotation.{ action, ignore, mapping, param, response, view, views }
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionConfig, ActionMapping, ActionMappingBuilder, Profile }
import org.beangle.webmvc.view.{ TemplateResolver, ViewBuilder }
import org.beangle.webmvc.view.impl.{ DefaultTemplatePathMapper, FreemarkerView }

@description("缺省的ActionMapping构建器")
class DefaultActionMappingBuilder extends ActionMappingBuilder {

  var templateResolver: TemplateResolver = _

  var viewBuilder: ViewBuilder = _

  var viewScan = true

  override def build(clazz: Class[_], profile: Profile): Seq[Tuple2[String, ActionMapping]] = {
    val nameAndspace = ActionNameBuilder.build(clazz, profile)
    val actionName = nameAndspace._1
    val actions = new collection.mutable.ListBuffer[Tuple2[String, ActionMapping]]
    val config = new ActionConfig(clazz, actionName, nameAndspace._2, buildViews(clazz, profile), profile)
    val mappings = new collection.mutable.HashMap[String, ActionMapping]
    val classInfo = ClassInfo.get(clazz)
    classInfo.methods foreach {
      case (methodName, minfos) =>
        val actionMethodInfos = minfos.filter(m => m.method.getDeclaringClass != classOf[ActionSupport] && isActionMethod(m.method, classInfo))
        if (actionMethodInfos.size == 1) {
          val method = actionMethodInfos.head.method
          val annTuple = getAnnotation(method, classOf[mapping])
          val ann = if (null == annTuple) null else annTuple._1
          val httpMethod = if (null != ann && isNotEmpty(ann.method)) ann.method.toUpperCase.intern else GET
          val name = (if (null != ann) ann.value else methodName)
          val url = if (name == "") actionName else (actionName + "/" + name)
          val urlParams = parse(url)
          val urlPathNames = urlParams.keySet.toList.sorted.map { i => urlParams(i) }

          val annotationsList = if (null == annTuple) method.getParameterAnnotations else annTuple._2.getParameterAnnotations

          val parameterTypes = method.getParameterTypes()
          val paramNames = Range(0, annotationsList.length) map { i =>
            annotationsList(i).find { ann => ann.isInstanceOf[param] } match {
              case Some(p) => p.asInstanceOf[param].value
              case None =>
                if (parameterTypes(i).getName == "javax.servlet.http.HttpServletRequest") "_request"
                else if (parameterTypes(i).getName == "javax.servlet.http.HttpServletResponse") "_response"
                else urlPathNames(i)
            }
          }

          if (method.getParameterTypes().length != paramNames.size) throw new RuntimeException("Cannot find enough param name,Using @mapping or @param")
          val mapping = new ActionMapping(httpMethod, config, method, name, paramNames.toArray, urlParams)
          mappings.put(method.getName, mapping)
          actions += Tuple2(url, mapping)
          if (name == "index" && method.getParameterTypes.length == 0) actions += Tuple2(actionName, mapping)
        }
    }
    config.mappings = mappings.toMap
    actions
  }

  def parse(pattern: String): Map[Integer, String] = {
    var parts = split(pattern, "/")
    var params = new collection.mutable.HashMap[Integer, String]
    var i = 0
    while (i < parts.length) {
      val p = parts(i)
      if (p.charAt(0) == '{' && p.charAt(p.length - 1) == '}') {
        params.put(Integer.valueOf(i), p.substring(1, p.length - 1))
      } else if (p == "*") {
        params.put(Integer.valueOf(i), String.valueOf(i))
      }
      i += 1
    }
    params.toMap
  }

  private def isActionMethod(method: Method, classInfo: ClassInfo): Boolean = {
    val methodName = method.getName
    if (methodName.startsWith("get") || methodName.contains("$")) return false
    //filter ignore
    if (null != getAnnotation(method, classOf[ignore])) return false

    if (null == getAnnotation(method, classOf[response])) {
      //filter method don't return string or view
      val returnType = method.getReturnType()
      if (returnType != classOf[String] && returnType != classOf[View]) return false
    }
    //filter field
    if (method.getParameterTypes.length == 0 && !classInfo.getMethods(methodName + "_$eq").isEmpty) return false
    true
  }

  protected def buildViews(clazz: Class[_], profile: Profile): Map[String, View] = {
    if (!viewScan) return Map.empty
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
        val m = mi.method
        var name = m.getName
        if (!annotationResults.contains(name) && shouldGenerateResult(m)) {
          val path = templateResolver.resolve(clazz, DefaultTemplatePathMapper.defaultView(name, name), suffix)
          if (null != path) viewMap.put(name, new FreemarkerView(path))
        }
      }
    }
    viewMap.toMap
  }

  protected def shouldGenerateResult(m: Method): Boolean = {
    if (classOf[String].equals(m.getReturnType) && !isAnnotationPresent(m, classOf[ignore])) {
      if (m.getParameterTypes.length == 0 || null != m.getAnnotation(classOf[mapping]) || containParamAnnotation(m.getParameterAnnotations)) {
        var name = m.getName.toLowerCase
        !(name.startsWith("save") || name.startsWith("remove")
          || name.startsWith("export") || name.startsWith("import")
          || name.startsWith("get") || Strings.contains(name, "$"))
      } else false
    } else false
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