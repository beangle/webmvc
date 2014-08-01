package org.beangle.webmvc.spring.mvc.view

class FreeMarkerViewResolver extends org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver {

  protected override def requiredViewClass(): Class[_] = {
    classOf[FreeMarkerView]
  }
}