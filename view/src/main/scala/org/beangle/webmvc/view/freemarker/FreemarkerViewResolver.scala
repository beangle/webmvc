package org.beangle.webmvc.view.freemarker

import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.RouteMapping
import org.beangle.webmvc.view.{ TemplateResolver, ViewResolver }

/**
 * @author chaostone
 */
@description("Freemaker视图解析器")
class FreemarkerViewResolver(templateResolver: TemplateResolver) extends ViewResolver {

  def resolve(actionClass: Class[_], viewName: String, suffix: String): View = {
    val path = templateResolver.resolve(actionClass, viewName, suffix)
    if (null == path) null else new FreemarkerView(path)
  }

  def resolve(viewName: String, mapping: RouteMapping): View = {
    val action = mapping.action
    val path = templateResolver.resolve(action.clazz, viewName, action.profile.viewSuffix)
    if (null == path) null else new FreemarkerView(path)
  }

  def supportViewType: String = {
    "freemarker"
  }
}