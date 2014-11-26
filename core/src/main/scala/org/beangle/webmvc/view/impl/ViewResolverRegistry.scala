package org.beangle.webmvc.view.impl

import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.context.LauncherListener
import org.beangle.webmvc.view.ViewResolver

class ViewResolverRegistry extends LauncherListener {

  var resolvers: Map[String, ViewResolver] = Map.empty

  override def start(container: Container): Unit = {
    val resolverMap = new collection.mutable.HashMap[String, ViewResolver]
    container.getBeans(classOf[ViewResolver]).values foreach { resolver =>
      resolverMap.put(resolver.supportViewType, resolver)
    }
    resolvers = resolverMap.toMap
  }

  def resolver(viewType: String): ViewResolver = {
    resolvers(viewType)
  }
}