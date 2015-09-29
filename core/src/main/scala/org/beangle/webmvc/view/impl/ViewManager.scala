package org.beangle.webmvc.view.impl

import org.beangle.commons.bean.Initializing
import org.beangle.commons.http.accept.ContentNegotiationManager
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.view.{ ViewRender, ViewResolver }
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.io.Serializer
import org.beangle.webmvc.view.TagLibraryProvider
import javax.activation.MimeType

@description("视图管理器")
class ViewManager extends Initializing {

  private var serializers: Map[String, Serializer] = _
  private var renders: Map[Class[_], ViewRender] = Map.empty

  var tagLibraryProvider: TagLibraryProvider = _
  var contentNegotiationManager: ContentNegotiationManager = _
  var container: Container = _
  var viewResolverRegistry: ViewResolverRegistry = _

  override def init(): Unit = {
    val renderMaps = new collection.mutable.HashMap[Class[_], ViewRender]
    container.getBeans(classOf[ViewRender]).values foreach { render =>
      renderMaps.put(render.supportViewClass, render)
    }
    renders = renderMaps.toMap
    val buf = new collection.mutable.HashMap[String, Serializer]
    container.getBeans(classOf[Serializer]) foreach {
      case (k, serializer) =>
        serializer.supportMediaTypes foreach { mimeType =>
          buf.put(mimeType.toString, serializer)
        }
    }
    serializers = buf.toMap
  }

  def getSerializer(mimeType: MimeType): Serializer = {
    serializers.get(mimeType.toString).orNull
  }

  def getResolver(viewType: String): Option[ViewResolver] = {
    viewResolverRegistry.getResolver(viewType)
  }

  def getRender(clazz: Class[_]): Option[ViewRender] = {
    renders.get(clazz)
  }
}