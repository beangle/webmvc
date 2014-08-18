package org.beangle.webmvc.config.impl

import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize }
import org.beangle.webmvc.api.annotation.action
import org.beangle.webmvc.config.{ Constants, Profile }

object ActionURIBuilder {

  def build(clazz: Class[_], profile: Profile): String = {
    val className = clazz.getName
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
    sb.toString
  }
}