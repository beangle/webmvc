package org.beangle.webmvc.route.impl

import java.lang.reflect.Method

import org.beangle.commons.lang.Arrays
import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize }
import org.beangle.commons.lang.reflect.ClassInfo
import org.beangle.webmvc.annotation.{ action, mapping, ignore }
import org.beangle.webmvc.route.{ Action, ActionBuilder, Constants, Profile, RouteService }

class DefaultActionBuilder(val routeService: RouteService) extends ActionBuilder {

  /**
   * 根据class对应的profile获得ctl/action类中除去后缀后的名字。<br>
   * 如果对应profile中是uriStyle,那么类中只保留简单类名，去掉后缀，并且小写第一个字母。<br>
   * 否则加上包名，其中的.编成URI路径分割符。包名不做其他处理。<br>
   * 复杂URL,以/开始
   */
  override def build(clazz: Class[_], method: String): Action = {
    val profile = routeService.getProfile(clazz.getName)
    val url = ActionURLBuilder.build(clazz, profile)
    val lastSlash = url.lastIndexOf('/')
    val result =Tuple2(url.substring(0, lastSlash), url.substring(lastSlash + 1))
    //FIXME consider annotation
    new Action(clazz, result._1, result._2, if (null == method) profile.defaultMethod else method).suffix(profile.uriSuffix)
  }
}