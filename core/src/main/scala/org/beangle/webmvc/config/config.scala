package org.beangle.webmvc.config

import java.lang.reflect.Method

trait Configurer {

  def getProfile(className: String): Profile
}