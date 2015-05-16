package org.beangle.webmvc.hibernate.action

import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.data.jpa.hibernate.ConfigurableSessionFactory
import org.beangle.webmvc.api.action.{ ParamSupport, RouteSupport }
import org.beangle.webmvc.hibernate.helper.SessionFactoryHelper
import org.hibernate.SessionFactory

abstract class AbstractAction extends RouteSupport with ParamSupport {

  var helper: SessionFactoryHelper = _

  def getSessionFactory(): SessionFactory = {
    val sfid = get("session_factory_id", "")
    if (isEmpty(sfid)) return null
    else helper.getSessionFactory(sfid)
  }

  def getFactory(): ConfigurableSessionFactory = {
    val sfid = get("session_factory_id", "")
    if (isEmpty(sfid)) return null
    else helper.getFactory(sfid)
  }

}