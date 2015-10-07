package org.beangle.webmvc.html2pdf

import java.io.OutputStream

import org.beangle.commons.activation.MimeTypes
import org.beangle.commons.io.Serializer

import javax.activation.MimeType

/**
 * @author chaostone
 */
class PdfSerializer extends Serializer {

  val pdfMimeType = new javax.activation.MimeType("application/pdf")

  override def serialize(data: AnyRef, os: OutputStream, params: Map[String, Any]): Unit = {

  }

  override def supportMediaTypes: Seq[MimeType] = {
    List(pdfMimeType)
  }
}