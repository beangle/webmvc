package org.beangle.webmvc.hibernate.action

import org.beangle.commons.lang.Strings.isEmpty
import org.beangle.spring.hibernate.LocalSessionFactoryBean
import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.hibernate.helper.SessionFactoryHelper
import org.hibernate.SessionFactory

abstract class AbstractAction extends ActionSupport {

  var helper: SessionFactoryHelper = _

  def getSessionFactory(): SessionFactory = {
    val sfid = get("session_factory_id", "")
    if (isEmpty(sfid)) return null
    else helper.getSessionFactory(sfid)
  }

  def getFactory(): LocalSessionFactoryBean = {
    val sfid = get("session_factory_id", "")
    if (isEmpty(sfid)) return null
    else helper.getFactory(sfid)
  }

}