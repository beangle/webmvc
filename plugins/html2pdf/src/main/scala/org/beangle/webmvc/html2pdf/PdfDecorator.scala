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
package org.beangle.webmvc.html2pdf

import java.io.OutputStream
import org.beangle.commons.activation.MimeTypes
import org.beangle.commons.io.Serializer
import javax.activation.MimeType
import org.beangle.webmvc.view.ViewDecorator
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.view.ViewResult
import java.io.ByteArrayOutputStream
import java.io.StringReader

/**
 * @author chaostone
 */
class PdfDecorator extends ViewDecorator {

  val reporter = new ITextPdfReporter
  val supportMimeType = MimeTypes.TextHtml.toString

  override def decorate(result: ViewResult, uri: String, context: ActionContext): ViewResult = {
    if (result.contentType.startsWith(supportMimeType) && uri.endsWith(".pdf")) {
      val reader = result.data match {
        case sb: StringBuffer => new StringReader(sb.toString)
        case s: String        => new StringReader(s)
        case _                => throw new RuntimeException("Cannot accept " + result.data.getClass)
      }
      val repcontext = new ReportContext
      val os = new ByteArrayOutputStream
      reporter.generate(reader, repcontext, os)
      os.close()
      ViewResult(os.toByteArray(), MimeTypes.ApplicationPdf.toString)
    } else {
      result
    }
  }
}