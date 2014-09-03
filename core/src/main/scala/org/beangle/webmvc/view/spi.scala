package org.beangle.webmvc.view

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.config.{ ActionMapping, Profile }

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

@spi
trait TagLibrary {
  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef
}

@spi
trait TagLibraryProvider {

  def tagLibraries: Map[String, TagLibrary]
}

@spi
trait TemplatePathMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}

@spi
trait TemplateResolver {
  def resolve(actionClass: Class[_], viewName: String, suffix: String): String
}

@spi
trait ViewRender {
  def supportViewClass: Class[_]
  def render(view: View, context: ActionContext)
}

/**
 * find view by mapping and viewName
 */
@spi
trait ViewResolver {
  def resolve(viewName: String, mapping: ActionMapping): View
  def supportViewType: String
}

@spi
trait TypeViewBuilder {
  def build(view: view): View
  def supportViewType: String
}

/**
 * Builder view from annotation
 */
@spi
trait ViewBuilder {
  def build(view: view, defaultType: String): View
}

trait LocatedView extends View {
  def location: String
}