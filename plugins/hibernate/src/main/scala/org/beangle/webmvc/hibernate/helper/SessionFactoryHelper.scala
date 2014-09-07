package org.beangle.webmvc.hibernate.helper

import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.data.jpa.hibernate.ConfigurableSessionFactory
import org.hibernate.SessionFactory

class SessionFactoryHelper extends Initializing {
  var factories: Map[Any, ConfigurableSessionFactory] = _

  var container: Container = _

  override def init() {
    factories = container.parent.getBeans(classOf[ConfigurableSessionFactory]).map {
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

  def getFactory(id: String): ConfigurableSessionFactory = {
    factories(id)
  }
}