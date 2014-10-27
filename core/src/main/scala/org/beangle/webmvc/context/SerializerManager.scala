package org.beangle.webmvc.context

import javax.activation.MimeType
import org.beangle.commons.io.Serializer

trait SerializerManager {

  def getSerializer(mimeType:MimeType):Serializer
}