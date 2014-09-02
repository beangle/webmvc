package org.beangle.webmvc.config.action

import org.beangle.webmvc.api.action.ActionSupport

class HibernateAction extends ActionSupport {
  def index(): String = {
    forward()
  }
}