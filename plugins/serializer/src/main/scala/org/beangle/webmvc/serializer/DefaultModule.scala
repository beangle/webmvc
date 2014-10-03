package org.beangle.webmvc.serializer

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.commons.http.accept.ContentNegotiationManagerFactory
import org.beangle.data.serializer.JsonSerializer
import org.beangle.data.serializer.XmlSerializer
import org.beangle.data.serializer.converter.DefaultConverterRegistry
import org.beangle.data.serializer.mapper.DefaultMapper
import org.beangle.data.serializer.io.xml.DomDriver
import org.beangle.data.serializer.io.json.DefaultJsonDriver
import org.beangle.data.serializer.AbstractSerializer

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[ContentNegotiationManagerFactory]).property("favorPathExtension", "true")
      .property("favorParameter", "true").property("parameterName", "format").property("ignoreAcceptHeader", "false")

    bind(classOf[DefaultConverterRegistry])
    bind(classOf[DefaultMapper])
    bind(classOf[DomDriver]).constructor("UTF-8")
    bind(classOf[DefaultJsonDriver]).constructor("UTF-8")
    bind("web.Serializer.xml", classOf[XmlSerializer]).property("mode", AbstractSerializer.SINGLE_NODE_XPATH_ABSOLUTE_REFERENCES)
    bind("web.Serializer.json", classOf[JsonSerializer]).property("mode", AbstractSerializer.SINGLE_NODE_XPATH_ABSOLUTE_REFERENCES )
  }
}