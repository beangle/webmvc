package org.beangle.webmvc.config.action

import org.beangle.webmvc.api.action.ActionSupport

class IndexAction extends ActionSupport{
  
  def index(): String = {
    forward()
  }
}