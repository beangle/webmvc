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

package org.beangle.webmvc.support.helper

import jakarta.servlet.http.Part
import org.beangle.commons.lang.Strings
import org.beangle.transfer.Format
import org.beangle.transfer.importer.{CsvReader, ExcelReader, Reader}
import org.beangle.webmvc.context.ActionContext

import java.io.{InputStreamReader, LineNumberReader}

object ImportHelper {

  def buildReader(upload: String = "importFile"): Reader = {
    val request = ActionContext.current.request
    val parts = request.getParts
    val partIter = parts.iterator
    var filePart: Part = null
    while (partIter.hasNext && null == filePart) {
      val part = partIter.next()
      if (part.getName == "importFile") filePart = part
    }
    if (null == filePart) {
      return null
    }
    val is = filePart.getInputStream
    var ext = Strings.substringAfterLast(filePart.getSubmittedFileName, ".")
    ext = Strings.capitalize(ext)
    if (ext != "Xlsx" && ext != "Xls") {
      return null
    }
    val format = Format.valueOf(ext)
    if (format.equals(Format.Xls) || format.equals(Format.Xlsx)) {
      new ExcelReader(is, 0, format)
    } else {
      val reader = new LineNumberReader(new InputStreamReader(is))
      new CsvReader(reader)
    }
  }
}
