package org.beangle.webmvc.view.tag

import javax.servlet.http.HttpServletResponse
import javax.servlet.http.HttpServletRequest

trait TagLibrary {
  def getModels(req: HttpServletRequest, res: HttpServletResponse): Any
}