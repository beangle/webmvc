package org.beangle.webmvc.showcase.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.view.{ View, status }

class StatusAction extends ActionSupport {

  def index(): View = {
    status(404)
  }

  def code403(): View = {
    status.forbidden
  }
}