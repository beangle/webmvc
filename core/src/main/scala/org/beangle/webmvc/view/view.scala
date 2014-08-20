package org.beangle.webmvc.view

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.Profile
import org.beangle.webmvc.dispatch.ActionMapping
@spi
trait ViewPathMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}

trait LocatedView extends View {
  def location: String
}

trait ViewRender {
  def supportView: Class[_]
  def render(view: View, context: ActionContext)
}

trait ViewResolver {
  def resolve(viewName: String, mapping: ActionMapping): View
}