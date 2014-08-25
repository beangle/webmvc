package org.beangle.webmvc.config.impl

import org.beangle.commons.lang.Strings.{ substringBeforeLast, unCamel, uncapitalize }
import org.beangle.webmvc.api.annotation.action
import org.beangle.webmvc.config.Profile

object ActionNameBuilder {

  /**
   * Return action name start with /
   */
  def build(clazz: Class[_], profile: Profile): String = {
    val className = clazz.getName
    val ann = clazz.getAnnotation(classOf[action])
    val sb = new StringBuilder()
    // namespace
    sb.append(profile.uriPath)

    if (null == ann) {
      if (Profile.SHORT_URI.equals(profile.uriStyle)) {
        val simpleName = className.substring(className.lastIndexOf('.') + 1)
        sb.append(uncapitalize(simpleName.substring(0, simpleName.length - profile.actionSuffix.length)))
      } else if (Profile.SIMPLE_URI.equals(profile.uriStyle)) {
        sb.append(profile.getMatched(className))
      } else if (Profile.SEO_URI.equals(profile.uriStyle)) {
        sb.append(unCamel(profile.getMatched(className)))
      } else {
        throw new RuntimeException("unsupported uri style " + profile.uriStyle)
      }
    } else {
      val name = ann.value()
      if (!name.startsWith("/")) {
        if (Profile.SEO_URI == profile.uriStyle) {
          sb.append(unCamel(substringBeforeLast(profile.getMatched(className), "/")) + "/" + name)
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