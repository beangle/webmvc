package org.beangle.webmvc.serializer

import org.beangle.commons.io.Serializer
import java.io.OutputStream
import javax.activation.MimeType
import org.beangle.commons.activation.MimeTypes
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.data.serializer.JsonSerializer

class JsonpSerializer extends Serializer {

  var jsonSerializer: JsonSerializer = _

  var callbackName = "callback"

  //FIXME use commons.core 4.1.1
  val TextScript = new MimeType("text/javascript")

  def serialize(data: AnyRef, os: OutputStream) = {
    val params = ContextHolder.context.params
    val callback = params.get(callbackName).getOrElse("callback").asInstanceOf[String]
    os.write(callback.getBytes())
    os.write('(')
    jsonSerializer.serialize(data, os)
    os.write(')')
  }

  def supportMediaTypes: Seq[MimeType] = {
    List(TextScript)
  }
}