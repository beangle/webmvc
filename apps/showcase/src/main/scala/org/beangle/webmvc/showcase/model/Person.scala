package org.beangle.webmvc.showcase.model

import java.io.File
import java.{ util => ju }
import org.beangle.data.model.bean.IntIdBean

class Person(var code: String, var name: String) extends IntIdBean {
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
