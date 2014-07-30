package org.beangle.webmvc.route

import javax.servlet.http.HttpServletRequest

object Constants {
  // 路径分割符
  val separator = '/'

  val SHORT_URI = "short"

  val SEO_URI = "seo"

  val SIMPLE_URI = "simple"

  val FULL_VIEWPATH = "full"

  val SEO_VIEWPATH = "seo"

  val SIMPLE_VIEWPATH = "simple"
}

trait RouteService {

  def getProfile(className: String): Profile

  def getProfile(clazz: Class[_]): Profile

  def profiles: List[Profile]

  def viewMapper: ViewMapper

  def actionBuilder: ActionBuilder
  /**
   * 默认类名对应的控制器名称(含有扩展名)
   */
  def buildAction(className: String): Action

  /**
   * viewname -> 页面路径的映射
   */
  def mapView(className: String, methodName: String, viewName: String): String

}
trait ViewMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def getViewPath(className: String, methodName: String, viewName: String): String
}

trait ActionBuilder {

  /**
   * 默认类名对应的控制器名称(含有扩展名)
   */
  def build(className: String): Action
}

trait RequestMapper {
  def resolve(request: HttpServletRequest): ActionMapping
}