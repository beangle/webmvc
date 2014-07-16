package org.beangle.webmvc.context

import scala.collection.mutable.ListBuffer

@SerialVersionUID(4112997123562877516L)
class ActionMessages extends Serializable {

  val messages = new ListBuffer[String]
  val errors = new ListBuffer[String]

  def clear() {
    messages.clear()
    errors.clear()
  }
}