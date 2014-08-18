package org.beangle.webmvc.spi.view.template

trait TemplateFinder {
  def find(actionClass: Class[_], viewName: String, suffix: String): String
}