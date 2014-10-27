package org.beangle.webmvc.view.impl

import org.beangle.webmvc.view.ViewBuilder
import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.webmvc.view.TypeViewBuilder
import org.beangle.webmvc.api.annotation.view
import org.beangle.webmvc.api.view.View
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.description

@description("缺省视图构建者")
class DefaultViewBuilder(container: Container) extends ViewBuilder {

  val builders = container.getBeans(classOf[TypeViewBuilder]).values.map { builder =>
    (builder.supportViewType, builder)
  }.toMap

  override def build(view: view, defaultType: String): View = {
    val viewType = if (Strings.isEmpty(view.`type`)) defaultType else view.`type`
    builders(viewType).build(view)
  }

}