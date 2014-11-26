package org.beangle.webmvc.view.freemarker

import org.beangle.commons.collection.IdentityCache
import org.beangle.template.freemarker.BeangleObjectWrapper
import org.beangle.webmvc.api.context.ContextHolder

import freemarker.template.TemplateModel

class CachedObjectWrapper(altMapWrapper: Boolean) extends BeangleObjectWrapper(altMapWrapper) {

  override def wrap(obj: AnyRef): TemplateModel = {
    if (null == obj) return null
    //FIXME need ab test
    val context = ContextHolder.context
    var models = context.temp[IdentityCache[AnyRef, TemplateModel]]("_TemplateModels")
    if (models == null) {
      models = new IdentityCache[AnyRef, TemplateModel]
      context.temp("_TemplateModels", models)
    }
    var model = models.get(obj)
    if (null != model) return model
    model = super.wrap(obj)
    models.put(obj, model)
    model
  }
}