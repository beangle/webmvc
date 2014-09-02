package org.beangle.webmvc.hibernate.action

import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.webmvc.api.action.ActionSupport
import org.hibernate.SessionFactory

class AbstractHibernateAction extends ActionSupport with Initializing {
  var factories: Map[Any, SessionFactory] = _
  var container: Container = _

  override def init() {
    factories = container.parent.getBeans(classOf[SessionFactory])
  }

  def getFactory(id: String = null): SessionFactory = {
    val sfid = if (null == id) get("id", "") else id
    if (isEmpty(sfid)) factories.values.head
    else factories(sfid)
  }

}