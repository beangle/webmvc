package org.beangle.webmvc.view

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.Profile
@spi
trait ViewMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}
