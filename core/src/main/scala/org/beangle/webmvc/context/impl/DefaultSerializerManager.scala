package org.beangle.webmvc.context.impl

import org.beangle.commons.inject.ContainerRefreshedHook
import org.beangle.webmvc.context.SerializerManager
import javax.activation.MimeType
import org.beangle.commons.io.Serializer
import org.beangle.commons.inject.Container

class DefaultSerializerManager extends SerializerManager with ContainerRefreshedHook {

  var serializers: Map[String, Serializer] = _

  def notify(container: Container): Unit = {
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