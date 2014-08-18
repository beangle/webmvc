package org.beangle.webmvc.view.tag

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse
import org.beangle.commons.text.i18n.TextResource
import java.util.HashMap
import scala.collection.JavaConversions._
import org.beangle.commons.text.i18n.spi.TextResourceProvider
import org.beangle.webmvc.view.bean.ActionUriRender
import org.beangle.webmvc.view.bean.IndexableIdGenerator
import org.beangle.webmvc.view.UITheme
import org.beangle.webmvc.view.template.Theme
import org.beangle.webmvc.view.component.ComponentContext
import org.beangle.commons.inject.Container
import org.beangle.webmvc.view.component.Component
import org.beangle.webmvc.view.template.Theme
import org.beangle.webmvc.view.template.Theme
import org.beangle.webmvc.api.context.ContextHolder

/**
 * New taglibrary.
 */
abstract class AbstractModels(val context: ComponentContext, request: HttpServletRequest) {

  val models = new HashMap[Class[_], TagModel]

  protected def get(clazz: Class[_ <: Component]): TagModel = {
    var model = models.get(clazz)
    if (null == model) {
      model = new TagModel(context, clazz)
      models.put(clazz, model)
    }
    return model
  }
}