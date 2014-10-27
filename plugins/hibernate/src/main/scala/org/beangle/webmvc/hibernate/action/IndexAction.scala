package org.beangle.webmvc.hibernate.action

class IndexAction extends AbstractAction {

  def index(): String = {
    put("factories", helper.factories)
    forward()
  }
}