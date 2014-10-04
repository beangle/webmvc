package org.beangle.webmvc.showcase.model

import java.io.File
import com.fasterxml.jackson.core.JsonEncoding
import com.fasterxml.jackson.databind.{ ObjectMapper, SerializationFeature }
import com.fasterxml.jackson.module.scala.DefaultScalaModule
import com.thoughtworks.xstream.XStream
import com.thoughtworks.xstream.io.xml.DomDriver
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver
import com.thoughtworks.xstream.io.HierarchicalStreamReader
import com.thoughtworks.xstream.converters.MarshallingContext
import com.thoughtworks.xstream.converters.collections.AbstractCollectionConverter
import com.thoughtworks.xstream.io.HierarchicalStreamWriter
import com.thoughtworks.xstream.mapper.Mapper
import com.thoughtworks.xstream.converters.UnmarshallingContext
import java.{ util => ju }
class Person(var code: String, var name: String) {
  var address = Address("minzu", "500", "jiading")
  var mobile: String = _
  var addresses = List(Address("minzu", "500", "jiading"), Address("minzu2", "5002", "jiading2"))
  var families = Map("wife" -> "a girl", "daught" -> "ketty")

}
trait Addressable {
  val name: String
  val street: String
  val city: String
}

case class Address(name: String, street: String, city: String) extends Addressable

object Person {

  def main(args: Array[String]) {
    val mapper = new ObjectMapper()
    mapper.registerModule(DefaultScalaModule)

    val os = new java.io.FileOutputStream(new File("/tmp/a.txt"))

    val encoding = JsonEncoding.UTF8
    // The following has been deprecated as late as Jackson 2.2 (April 2013);
    // preserved for the time being, for Jackson 2.0/2.1 compatibility.
    val jsonGenerator =
      mapper.getJsonFactory().createJsonGenerator(os, encoding)

    // A workaround for JsonGenerators not applying serialization features
    // https://github.com/FasterXML/jackson-databind/issues/12
    if (mapper.isEnabled(SerializationFeature.INDENT_OUTPUT)) {
      jsonGenerator.useDefaultPrettyPrinter()
    }

    mapper.writeValue(jsonGenerator, List(new Person("001", "admin"), new Person("002", "admin2")))
    mapper.writeValue(jsonGenerator, Address("001", "admin", "xx"))
    mapper.writeValue(jsonGenerator, Map(("001", "value1"), ("002", "value2")))

    //val serializer = new MarshallingSerializer 
    //    serializer.marshaller =  
    //    val xstream = XStreamConversions(new XStream(new DomDriver()))

    // make Topping and Pizza lowercase

    //    val xstream = new XStream(new JsonHierarchicalStreamDriver)
    val xstream = new XStream(new JsonHierarchicalStreamDriver)
    ListConverter.configureXStream(xstream)
    xstream.alias("person", classOf[Person])
    xstream.alias("address", classOf[Address])
    println(xstream.toXML(List(new Person("002", "admin2"), new Person("001", "admin"))))
  }
}

class ListConverter(_mapper: Mapper) extends AbstractCollectionConverter(_mapper) {
  /**
   * Helper method to use x.getClass
   *
   * See: http://scalide.blogspot.com/2009/06/getanyclass-tip.html
   */
  def getAnyClass(x: Any) = x.asInstanceOf[AnyRef].getClass

  def canConvert(clazz: Class[_]) = {
    classOf[::[_]] == clazz
  }

  def marshal(value: Any, writer: HierarchicalStreamWriter, context: MarshallingContext) = {
    val list = value.asInstanceOf[List[_]]
    for (item <- list) {
      writeItem(item, context, writer)
    }
  }

  def unmarshal(reader: HierarchicalStreamReader, context: UnmarshallingContext) = {
    println(context.getRequiredType())
    var list: List[_] = createCollection(context.getRequiredType()).asInstanceOf[List[_]]
    while (reader.hasMoreChildren()) {
      reader.moveDown();
      val item = readItem(reader, context, list);
      list = item :: list
      reader.moveUp();
    }
    list
  }
}

object ListConverter {
  def configureXStream(stream: XStream) = {
    stream.alias("list", classOf[::[_]])
    stream.registerConverter(new ListConverter(stream.getMapper))
  }
}