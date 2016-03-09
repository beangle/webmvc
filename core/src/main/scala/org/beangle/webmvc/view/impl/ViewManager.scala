/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
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
package org.beangle.webmvc.view.impl

import org.beangle.commons.http.accept.ContentNegotiationManager
import org.beangle.commons.inject.{ Container, ContainerListener }
import org.beangle.commons.io.Serializer
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.view.{ ViewRender, ViewResolver }

import javax.activation.MimeType

@description("视图管理器")
class ViewManager extends ContainerListener {

  private var serializers: Map[String, Serializer] = _
  private var renders: Map[Class[_], ViewRender] = Map.empty
  private var resolvers: Map[String, ViewResolver] = Map.empty

  var contentNegotiationManager: ContentNegotiationManager = _

  override def onStarted(container: Container): Unit = {
    val renderMaps = new collection.mutable.HashMap[Class[_], ViewRender]
    container.getBeans(classOf[ViewRender]).values foreach { render =>
      renderMaps.put(render.supportViewClass, render)
    }
    renders = renderMaps.toMap

    val resolverMap = new collection.mutable.HashMap[String, ViewResolver]
    container.getBeans(classOf[ViewResolver]).values foreach { resolver =>
      resolverMap.put(resolver.supportViewType, resolver)
    }
    resolvers = resolverMap.toMap

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
    resolvers.get(viewType)
  }

  def getRender(clazz: Class[_]): Option[ViewRender] = {
    renders.get(clazz)
  }
}