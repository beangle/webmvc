package org.beangle.webmvc.route

/**
 * viewname -> 页面路径的映射
 */
trait ViewMapper {

  def getViewPath(className: String, methodName: String, viewName: String): String

}