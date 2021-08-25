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

package org.beangle.webmvc.support.pdf

import org.beangle.commons.activation.MediaTypes
import org.beangle.web.action.context.ActionContext
import org.beangle.webmvc.view.{ViewDecorator, ViewResult}

import java.io.{ByteArrayOutputStream, StringReader}

/**
 * @author chaostone
 */
class PdfDecorator extends ViewDecorator {

  val reporter = new ITextPdfReporter
  val supportMimeType: String = MediaTypes.TextHtml.toString

  override def decorate(result: ViewResult, uri: String, context: ActionContext): ViewResult = {
    if (result.contentType.startsWith(supportMimeType) && uri.endsWith(".pdf")) {
      val reader = result.data match {
        case sb: StringBuffer => new StringReader(sb.toString)
        case s: String => new StringReader(s)
        case _ => throw new RuntimeException("Cannot accept " + result.data.getClass)
      }
      val repcontext = new ReportContext
      val os = new ByteArrayOutputStream
      reporter.generate(reader, repcontext, os)
      os.close()
      ViewResult(os.toByteArray, MediaTypes.ApplicationPdf.toString)
    } else {
      result
    }
  }
}
