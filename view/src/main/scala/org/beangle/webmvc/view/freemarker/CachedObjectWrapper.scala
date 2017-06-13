/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.freemarker

import org.beangle.commons.collection.IdentityCache
import org.beangle.template.freemarker.BeangleObjectWrapper
import org.beangle.webmvc.api.context.ActionContext

import freemarker.template.TemplateModel

class CachedObjectWrapper extends BeangleObjectWrapper {

  override def wrap(obj: AnyRef): TemplateModel = {
    if (null == obj) return null
    //FIXME need ab test
    val context = ActionContext.current
    var models = context.stash[IdentityCache[AnyRef, TemplateModel]]("_TemplateModels")
    if (models == null) {
      models = new IdentityCache[AnyRef, TemplateModel]
      context.stash("_TemplateModels", models)
    }
    var model = models.get(obj)
    if (null != model) return model
    model = super.wrap(obj)
    models.put(obj, model)
    model
  }
}
