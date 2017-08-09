/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.stream

import org.beangle.cdi.bind.BindModule
import org.beangle.commons.io.DefaultBinarySerializer
import org.beangle.commons.web.http.accept.ContentNegotiationManagerFactory
import org.beangle.data.stream.{ CsvSerializer, JsonSerializer, JsonpSerializer, XmlSerializer }
import org.beangle.data.stream.io.csv.DefaultCsvDriver
import org.beangle.data.stream.io.json.DefaultJsonDriver
import org.beangle.data.stream.io.jsonp.DefaultJsonpDriver
import org.beangle.data.stream.io.xml.DomDriver
import org.beangle.data.stream.mapper.DefaultMapper
import org.beangle.data.stream.marshal.DefaultMarshallerRegistry

object DefaultModule extends BindModule {

  protected override def binding() {
    bind(classOf[ContentNegotiationManagerFactory]).property("favorPathExtension", "true")
      .property("favorParameter", "true").property("parameterName", "format").property("ignoreAcceptHeader", "false")

    bind(classOf[DefaultMarshallerRegistry])
    bind(classOf[DefaultMapper])
    bind(classOf[DomDriver]).constructor("UTF-8")
    bind(classOf[DefaultJsonDriver]).constructor("UTF-8")
    bind(classOf[DefaultJsonpDriver]).constructor("UTF-8")
    bind(classOf[DefaultCsvDriver]).constructor("UTF-8")

    bind("web.Serializer.xml", classOf[XmlSerializer])
    bind("web.Serializer.json", classOf[JsonSerializer])
    bind("web.Serializer.jsonp", classOf[JsonpSerializer])
    bind("web.Serializer.csv", classOf[CsvSerializer])
    bind("web.Serializer.bin", DefaultBinarySerializer)
  }
}
