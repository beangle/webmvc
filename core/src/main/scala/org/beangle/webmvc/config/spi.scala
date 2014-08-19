package org.beangle.webmvc.config

import org.beangle.commons.lang.annotation.spi

@spi
trait ProfileProvider {

  def loadProfiles(): List[Profile]
}
