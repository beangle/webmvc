package org.beangle.webmvc.config

import org.beangle.commons.lang.annotation.spi
import java.lang.reflect.Method

@spi
trait ProfileProvider {

  def loadProfiles(): List[Profile]
}

@spi
trait ActionMappingBuilder {

  def build(clazz: Class[_], profile: Profile): Seq[Tuple2[String, ActionMapping]]
}
