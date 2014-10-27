package org.beangle.webmvc.config

import org.beangle.commons.lang.annotation.spi
import java.lang.reflect.Method

@spi
trait ProfileProvider {

  def loadProfiles(): List[ProfileConfig]
}

@spi
trait ActionMappingBuilder {

  /**
   * build mapping url  mapping(with httpmethod)
   */
  def build(clazz: Class[_], profile: Profile): Seq[Tuple2[String, ActionMapping]]
}
