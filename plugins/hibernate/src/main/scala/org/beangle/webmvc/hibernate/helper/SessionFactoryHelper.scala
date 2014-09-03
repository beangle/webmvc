package org.beangle.webmvc.hibernate.helper

import org.hibernate.SessionFactory
import org.beangle.spring.hibernate.LocalSessionFactoryBean
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.commons.bean.Initializing

class SessionFactoryHelper extends Initializing {
  var factories: Map[Any, LocalSessionFactoryBean] = _

  var container: Container = _

  override def init() {
    factories = container.parent.getBeans(classOf[LocalSessionFactoryBean]).map {
      case (k, v) =>
        var name = k.toString
        name = name.replace(".", "_")
        name = name.replace("#", "_")
        (name, v)
    }
  }

  def getSessionFactory(id: String): SessionFactory = {
    factories(id).result
  }

  def getFactory(id: String): LocalSessionFactoryBean = {
    factories(id)
  }
}