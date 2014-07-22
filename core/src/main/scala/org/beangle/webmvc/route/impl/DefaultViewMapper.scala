package org.beangle.webmvc.route.impl

import org.beangle.commons.lang.Strings
import org.beangle.webmvc.route.{Constants, RouteService, ViewMapper}

class DefaultViewMapper(val routeService: RouteService) extends ViewMapper {

  private val methodViews = Map(("search", "list"), ("query", "list"),
    ("edit", "form"), ("home", "index"), ("execute", "index"), ("add", "new"))

  /**
   * 查询control对应的view的名字(没有后缀)
   */
  def getViewPath(className: String, methodName: String, viewName: String): String = {
    if (Strings.isNotEmpty(viewName) && viewName.charAt(0) == Constants.separator) viewName
    else {
      val newViewName = {
        if (Strings.isEmpty(viewName) || viewName.equals("success")) {
          methodName
        } else viewName
      }
      val profile = routeService.getProfile(className)
      if (null == profile) { throw new RuntimeException("no convention profile for " + className) }
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

      buf.append(methodViews.get(newViewName).getOrElse(newViewName))
      buf.toString()
    }
  }
}