package org.beangle.webmvc.config

import java.lang.reflect.Method

trait Configurer {

  def getProfile(className: String): Profile

  //FIXME
  def buildMappings(clazz: Class[_]): Seq[Tuple2[ActionMapping, Method]]
}