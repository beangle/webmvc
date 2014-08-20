package org.beangle.webmvc.view.template

trait TemplateResolver {
  def find(actionClass: Class[_], viewName: String, suffix: String): String
}