package org.beangle.webmvc.view.template

trait TemplateFinder {
  def find(actionClass: Class[_], viewName: String, suffix: String): String
}