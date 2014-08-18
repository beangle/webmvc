package org.beangle.webmvc.spi.view

import org.beangle.webmvc.config.Profile

trait ViewMapper {
  /**
   * viewname -> 页面路径的映射
   */
  def map(className: String, viewName: String, profile: Profile): String
}