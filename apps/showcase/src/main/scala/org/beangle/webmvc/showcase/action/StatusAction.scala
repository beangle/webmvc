package org.beangle.webmvc.showcase.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.webmvc.api.view.{ View, Status }

class StatusAction extends ActionSupport {

  def index(): View = {
    Status(404)
  }

  def code403(): View = {
    Status.Forbidden
  }
  
  def json():String={
    forward()
  }
}