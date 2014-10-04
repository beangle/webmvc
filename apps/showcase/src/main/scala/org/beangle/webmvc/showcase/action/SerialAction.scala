package org.beangle.webmvc.showcase.action

import org.beangle.webmvc.api.annotation.response
import org.beangle.webmvc.showcase.model.Person

class SerialAction {

  @response
  def person(): Person = {
    new Person("001", "admin")
  }
  @response
  def persons(): List[Person] = {
    List(new Person("001", "admin"), new Person("007", "band"))
  }
}