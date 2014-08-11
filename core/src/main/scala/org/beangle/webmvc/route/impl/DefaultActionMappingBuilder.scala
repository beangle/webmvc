package org.beangle.webmvc.route.impl

import java.lang.reflect.Method

import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.annotation.{ ignore, mapping }
import org.beangle.webmvc.route.{ ActionMapping, ActionMappingBuilder, RouteService }

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
            val actionMethodName =
              if (null != ann) {
                if (null != httpMethod) ann.value + "/" + httpMethod.toLowerCase() else ann.value
              } else methodName
            actions += Tuple2(new ActionMapping(httpMethod, result + "/" + actionMethodName, namespace, name), method)
          }
        }
    }
    actions
  }

  private def isActionMethod(method: Method): Boolean = {
    val methodName = method.getName
    if (methodName.startsWith("get") || methodName.startsWith("debug") || methodName.contains("$")) return false
    if (null != method.getAnnotation(classOf[ignore])) return false
    if (method.getParameterTypes.length == 0) return true
    (null != method.getAnnotation(classOf[mapping])) || method.getParameterAnnotations().exists(annArray => !Arrays.isBlank(annArray))
  }

}