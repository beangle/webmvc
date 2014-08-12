package org.beangle.webmvc.route.impl

import java.lang.reflect.Method
import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.annotation.{ ignore, mapping }
import org.beangle.webmvc.route.{ ActionMapping, ActionMappingBuilder, RouteService }
import org.beangle.webmvc.annotation.action
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.annotation.param

class DefaultActionMappingBuilder(val routeService: RouteService) extends ActionMappingBuilder {

  override def build(clazz: Class[_]): Seq[Tuple2[ActionMapping, Method]] = {
    val profile = routeService.getProfile(clazz.getName)
    val result = ActionURLBuilder.build(clazz, profile)
    val lastSlash = result.lastIndexOf('/')
    val namespace = result.substring(0, lastSlash).intern()
    val name = result.substring(lastSlash + 1).intern()
    val actions = new collection.mutable.ListBuffer[Tuple2[ActionMapping, Method]]
    ClassInfo.get(clazz).methods foreach {
      case (methodName, minfos) =>
        if (minfos.size == 1) {
          val method = minfos.head.method
          if (isActionMethod(method)) {
            val ann = method.getAnnotation(classOf[mapping])
            val httpMethod = if (null != ann && ann.method != "") ann.method.toUpperCase.intern else null
            val url = result + "/" + (if (null != ann) ann.value else methodName)
            val urlParams = parse(url)
            val urlPathNames = urlParams.keySet.toList.sorted.map { i => urlParams(i) }

            val annotationsList = method.getParameterAnnotations
            val paramNames = Range(0, annotationsList.length) map { i =>
              annotationsList(i).find { ann => ann.isInstanceOf[param] } match {
                case Some(p) => p.asInstanceOf[param].value
                case None => urlPathNames(i)
              }
            }

            if (method.getParameterTypes().length != paramNames.size) throw new RuntimeException("Cannot find enough param name,Using @mapping or @param")

            actions += Tuple2(new ActionMapping(httpMethod, url, clazz, methodName, paramNames.toArray, urlParams, namespace, name), method)
          }
        }
    }
    actions
  }

  def parse(pattern: String): Map[Integer, String] = {
    var parts = Strings.split(pattern, "/")
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
  private def isActionMethod(method: Method): Boolean = {
    val methodName = method.getName
    if (methodName.startsWith("get") || methodName.startsWith("debug") || methodName.contains("$")) return false
    if (null != method.getAnnotation(classOf[ignore])) return false
    if (method.getParameterTypes.length == 0) return true
    (null != method.getAnnotation(classOf[mapping])) || method.getParameterAnnotations().exists(annArray => !Arrays.isBlank(annArray))
  }

}