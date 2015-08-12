package org.beangle.webmvc.view.freemarker

import freemarker.template.SimpleHash
import freemarker.template.TemplateModel
import freemarker.template.ObjectWrapper
import javax.servlet.http.HttpServletRequest

/**
 * Just extract value from default scope and request(omit session/context)
 */
class SimpleHttpScopesHashModel(wrapper: ObjectWrapper, val request: HttpServletRequest) extends SimpleHash(wrapper) {

  override def get(key: String): TemplateModel = {
    // Lookup in page scope
    val model = super.get(key);
    if (model != null) {
      return model;
    }

    // Lookup in request scope
    val obj = request.getAttribute(key);
    if (obj != null) {
      return wrap(obj);
    }
    // return wrapper's null object (probably null).        
    return wrap(null);
  }
}