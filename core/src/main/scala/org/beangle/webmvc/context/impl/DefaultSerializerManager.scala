/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
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
package org.beangle.webmvc.context.impl

import org.beangle.webmvc.context.SerializerManager
import javax.activation.MimeType
import org.beangle.commons.io.Serializer
import org.beangle.commons.inject.Container
import org.beangle.webmvc.context.LauncherListener

class DefaultSerializerManager extends SerializerManager {

  var serializers: Map[String, Serializer] = _

  override def start(container: Container): Unit = {
    val buf = new collection.mutable.HashMap[String, Serializer]
    container.getBeans(classOf[Serializer]) foreach { case(k,serializer) =>
      serializer.supportMediaTypes foreach { mimeType =>
        buf.put(mimeType.toString, serializer)
      }
    }
    serializers = buf.toMap
  }
  
  def getSerializer(mimeType: MimeType): Serializer = {
    serializers.get(mimeType.toString).orNull
  }
}