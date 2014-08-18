package org.beangle.webmvc.spi.config

import org.beangle.webmvc.config.Profile

trait ProfileProvider {

  def loadProfiles(): List[Profile]
}
