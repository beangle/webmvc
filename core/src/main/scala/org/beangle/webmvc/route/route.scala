package org.beangle.webmvc.route

import java.lang.reflect.Method

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

  def profiles: List[Profile]

  def viewMapper: ViewMapper

  /**
   * 默认类名对应的控制器名称(含有扩展名)
   */
  def buildAction(clazz: Class[_]): StrutsAction

  def buildMappings(clazz: Class[_]): Seq[Tuple2[ActionMapping, Method]]

  /**
   * viewname -> 页面路径的映射
   */
  def mapView(className: String, viewName: String): String

}

trait ViewMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}

trait Handler {
  def action: AnyRef
  def handle(mapping: ActionMapping): Any
}

object RequestMapper {
  final val DefaultMethod = "index"
  final val MethodParam = "_method"
  import org.beangle.commons.http.HttpMethods.{ DELETE, GET, HEAD, POST, PUT }
  final val HttpMethodMap = Map((GET, ""), (POST, ""), (PUT, "put"), (DELETE, "delete"), (HEAD, "head"))
  final val HttpMethods = Set("put", "delete", "head")

}

trait RequestMapper {

  def resolve(request: HttpServletRequest): Option[RequestMapping]

  def antiResolve(clazz: Class[_], method: String): Option[RequestMapping]

  def resolve(uri: String): Option[RequestMapping]

}