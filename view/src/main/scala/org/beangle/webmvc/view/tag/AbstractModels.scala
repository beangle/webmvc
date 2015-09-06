package org.beangle.webmvc.view.tag

import java.util.HashMap
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.view.tag.freemarker.TagModel

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