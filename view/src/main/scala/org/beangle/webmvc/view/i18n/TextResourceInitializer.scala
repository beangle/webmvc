package org.beangle.webmvc.view.i18n

import org.beangle.webmvc.context.ActionContextInitializer
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.api.i18n.TextProvider

/**
 * @author chaostone
 */
class TextResourceInitializer extends ActionContextInitializer {
  var textResourceProvider: ActionTextResourceProvider = _

  def init(context: ActionContext): Unit = {
    context.textProvider = Some(textResourceProvider.getTextResource(context.locale).asInstanceOf[TextProvider])
  }
}