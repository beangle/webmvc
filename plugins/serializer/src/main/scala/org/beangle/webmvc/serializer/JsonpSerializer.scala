package org.beangle.webmvc.serializer

import java.io.OutputStream

import org.beangle.commons.activation.MimeTypes
import org.beangle.commons.io.Serializer
import org.beangle.data.serializer.JsonSerializer
import org.beangle.webmvc.api.context.ContextHolder

import javax.activation.MimeType

class JsonpSerializer extends Serializer {

  var jsonSerializer: JsonSerializer = _

  var callbackName = "callback"

  def serialize(data: AnyRef, os: OutputStream) = {
    val params = ContextHolder.context.params
    val callback = params.get(callbackName).getOrElse("callback").asInstanceOf[String]
    os.write(callback.getBytes())
    os.write('(')
    jsonSerializer.serialize(data, os)
    os.write(')')
  }

  def supportMediaTypes: Seq[MimeType] = {
    List(MimeTypes.TextJavaScript)
  }
}