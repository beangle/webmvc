package org.beangle.webmvc.view.freemarker

import freemarker.template.{ ObjectWrapper, TemplateHashModel, TemplateModel }
import javax.servlet.ServletContext

/**
 * Just replace freemarker ServletContextHashModel
 */
class ServletContextHashModel(context: ServletContext, wrapper: ObjectWrapper) extends TemplateHashModel {

  override def get(key: String): TemplateModel = {
    wrapper.wrap(context.getAttribute(key))
  }

  override def isEmpty: Boolean = {
    context.getAttributeNames.hasMoreElements
  }

}
