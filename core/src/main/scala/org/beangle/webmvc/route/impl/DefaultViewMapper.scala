package org.beangle.webmvc.route.impl

import org.beangle.commons.lang.Strings
import org.beangle.webmvc.route.{ Constants, Profile, ViewMapper }

object DefaultViewMapper {
  private val methodViews = Map(("search", "list"), ("query", "list"), ("edit", "form"), ("home", "index"), ("execute", "index"), ("add", "new"))

  def defaultView(methodName: String, viewName: String): String = {
    if (null == viewName || "success" == viewName) methodViews.getOrElse(methodName, methodName)
    else methodViews.getOrElse(viewName, viewName)
  }

}
class DefaultViewMapper extends ViewMapper {

  /**
   * 查询control对应的view的名字(没有后缀)
   */
  def map(className: String, viewName: String, profile: Profile): String = {
    if (Strings.isNotEmpty(viewName) && viewName.charAt(0) == Constants.separator) viewName
    else {
      val buf = new StringBuilder()
      if (profile.viewPathStyle.equals(Constants.FULL_VIEWPATH)) {
        buf.append(Constants.separator)
        buf.append(profile.getFullPath(className))
      } else if (profile.viewPathStyle.equals(Constants.SIMPLE_VIEWPATH)) {
        buf.append(profile.viewPath)
        // 添加中缀路径
        buf.append(profile.getInfix(className))
      } else if (profile.viewPathStyle.equals(Constants.SEO_VIEWPATH)) {
        buf.append(profile.viewPath)
        buf.append(Strings.unCamel(profile.getInfix(className)))
      } else {
        throw new RuntimeException(profile.viewPathStyle + " was not supported")
      }
      // add method mapping path
      buf.append(Constants.separator)

      buf.append(viewName)
      buf.toString()
    }
  }
}