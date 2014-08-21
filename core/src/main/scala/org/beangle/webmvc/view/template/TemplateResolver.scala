package org.beangle.webmvc.view.template

trait TemplateResolver {
  def resolve(actionClass: Class[_], viewName: String, suffix: String): String
}