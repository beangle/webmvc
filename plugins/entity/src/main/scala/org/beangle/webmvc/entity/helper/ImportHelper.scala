/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.entity.helper

import javax.servlet.http.Part
import java.io.{LineNumberReader,InputStreamReader}

import org.beangle.data.model.Entity
import org.beangle.data.transfer.importer.{ EntityImporter, ImporterFactory, ImportResult }
import org.beangle.data.transfer.Format
import org.beangle.data.transfer.importer.listener.ForeignerListener
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.data.transfer.importer.{ ImportListener, ImportSetting, DefaultEntityImporter }
import org.beangle.data.transfer.csv.CsvItemReader
import org.beangle.data.transfer.excel.ExcelItemReader
import org.beangle.data.transfer.io.Reader

import org.beangle.webmvc.api.view.View
import org.beangle.commons.lang.Strings

object ImportHelper {

  def buildReader(upload: String = "importFile"): Reader = {
    val request = ActionContext.current.request
    val parts = request.getParts
    val partIter = parts.iterator
    var filePart: Part = null
    while (partIter.hasNext() && null == filePart) {
      val part = partIter.next()
      if (part.getName == "importFile") filePart = part
    }
    if (null == filePart) {
      return null
    }
    val is = filePart.getInputStream
    val format = Format.withName("Xls")

    if (format.equals(Format.Xls)) {
      new ExcelItemReader(is, 1)
    } else {
      val reader = new LineNumberReader(new InputStreamReader(is))
      new CsvItemReader(reader)
    }
  }
}
