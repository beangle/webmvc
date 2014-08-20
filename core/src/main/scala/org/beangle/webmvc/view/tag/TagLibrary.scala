package org.beangle.webmvc.view.tag

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest
import org.beangle.commons.lang.annotation.spi
@spi
trait TagLibrary {
  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef
}