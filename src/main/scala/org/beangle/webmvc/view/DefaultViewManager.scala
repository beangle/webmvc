/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.view

import org.beangle.commons.activation.MediaType
import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.Serializer
import org.beangle.commons.lang.annotation.description
import org.beangle.web.servlet.http.accept.ContentNegotiationManager

@description("视图管理器")
class DefaultViewManager extends Initializing {

  private var serializerMap: Map[String, Serializer] = Map.empty
  private var renderMap: Map[Class[_], ViewRender] = Map.empty
  private var resolverMap: Map[String, ViewResolver] = Map.empty

  var contentNegotiationManager: ContentNegotiationManager = _

  var viewRenders: List[ViewRender] = List.empty
  var viewResolvers: List[ViewResolver] = List.empty
  var serializers: List[Serializer] = List.empty

  override def init(): Unit = {
    val renderMaps = new collection.mutable.HashMap[Class[_], ViewRender]
    renderMap = viewRenders.map(x => (x.supportViewClass, x)).toMap
    resolverMap = viewResolvers.map(x => (x.supportViewType, x)).toMap
    val buf = new collection.mutable.HashMap[String, Serializer]
    serializers foreach { serializer =>
      serializer.mediaTypes foreach { mimeType =>
        buf.put(mimeType.toString, serializer)
      }
    }
    serializerMap = buf.toMap
  }

  def getSerializer(mimeType: MediaType): Serializer = {
    serializerMap.get(mimeType.toString).orNull
  }

  def getResolver(viewType: String): Option[ViewResolver] = {
    resolverMap.get(viewType)
  }

  def getRender(clazz: Class[_]): Option[ViewRender] = {
    renderMap.get(clazz)
  }
}
