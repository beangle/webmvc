package org.beangle.webmvc.config.action

import org.beangle.webmvc.api.action.ActionSupport

class WebinitAction extends ActionSupport {

    def index(): String = {
      forward()
    }
}