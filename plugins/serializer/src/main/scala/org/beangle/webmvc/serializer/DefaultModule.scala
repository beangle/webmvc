package org.beangle.webmvc.serializer

import org.beangle.commons.http.accept.ContentNegotiationManagerFactory
import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.data.serialize.{ CsvSerializer, JsonSerializer, JsonpSerializer, XmlSerializer }
import org.beangle.data.serialize.io.csv.DefaultCsvDriver
import org.beangle.data.serialize.io.json.DefaultJsonDriver
import org.beangle.data.serialize.io.jsonp.DefaultJsonpDriver
import org.beangle.data.serialize.io.xml.DomDriver
import org.beangle.data.serialize.mapper.DefaultMapper
import org.beangle.data.serialize.marshal.DefaultMarshallerRegistry

class DefaultModule extends AbstractBindModule {

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
  }
}