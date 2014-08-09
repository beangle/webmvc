package org.beangle.webmvc.route.impl

import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize }
import org.beangle.webmvc.annotation.action
import org.beangle.webmvc.route.{ Action, ActionBuilder, Constants, RouteService }
import org.beangle.webmvc.route.Profile
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.annotation.path
import org.beangle.commons.lang.Strings

class DefaultActionBuilder(val routeService: RouteService) extends ActionBuilder {

  /**
   * 根据class对应的profile获得ctl/action类中除去后缀后的名字。<br>
   * 如果对应profile中是uriStyle,那么类中只保留简单类名，去掉后缀，并且小写第一个字母。<br>
   * 否则加上包名，其中的.编成URI路径分割符。包名不做其他处理。<br>
   * 复杂URL,以/开始
   */
  def build(clazz: Class[_], method: String): Action = {
    val profile = routeService.getProfile(clazz.getName)
    val result = buildAction(clazz, profile)
    new Action(clazz, result._1, result._2, if (null == method) profile.defaultMethod else method).suffix(profile.uriSuffix)
  }

  def build(clazz: Class[_]): Seq[Action] = {
    val profile = routeService.getProfile(clazz.getName)
    val result = buildAction(clazz, profile)
    val actions = new collection.mutable.ListBuffer[Action]
    //FIXME too many method
    ClassInfo.get(clazz).getMethods foreach { minfo =>
      val methodName = minfo.method.getName
      if (!methodName.startsWith("get")) {
        val ann = minfo.method.getAnnotation(classOf[path])
        actions += new Action(clazz, result._1, result._2, (if (null != ann) ann.value() else methodName)).suffix(profile.uriSuffix)
      }
    }
    //FIXME already have index
    actions += new Action(clazz, result._1, result._2, profile.defaultMethod).suffix(profile.uriSuffix)
    actions
  }

  private def buildAction(clazz: Class[_], profile: Profile): Tuple2[String, String] = {
    val className = clazz.getName
    val profile = routeService.getProfile(className)
    val ann = clazz.getAnnotation(classOf[action])
    val sb = new StringBuilder()
    // namespace
    sb.append(profile.uriPath)

    if (null == ann) {
      if (Constants.SHORT_URI.equals(profile.uriPathStyle)) {
        val simpleName = className.substring(className.lastIndexOf('.') + 1)
        sb.append(uncapitalize(simpleName.substring(0, simpleName.length - profile.actionSuffix.length)))
      } else if (Constants.SIMPLE_URI.equals(profile.uriPathStyle)) {
        sb.append(profile.getInfix(className))
      } else if (Constants.SEO_URI.equals(profile.uriPathStyle)) {
        sb.append(unCamel(profile.getInfix(className)))
      } else {
        throw new RuntimeException("unsupported uri style " + profile.uriPathStyle)
      }
    } else {
      val name = ann.value()
      if (!name.startsWith("/")) {
        if (Constants.SEO_URI == profile.uriPathStyle) {
          sb.append(unCamel(substringBeforeLast(profile.getInfix(className), "/")) + "/" + name)
        } else {
          sb.append(name)
        }
      } else {
        sb.append(name.substring(1))
      }
    }
    val result = sb.toString
    val lastSlash = result.lastIndexOf('/')
    Tuple2(result.substring(0, lastSlash), result.substring(lastSlash + 1))
  }
}