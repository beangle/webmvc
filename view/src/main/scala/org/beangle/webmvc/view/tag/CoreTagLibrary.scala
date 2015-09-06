package org.beangle.webmvc.view.tag

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.dispatch.ActionUriRender
import org.beangle.webmvc.view.impl.IndexableIdGenerator
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
import org.beangle.commons.lang.annotation.description
import org.beangle.webmvc.view.TagLibrary

/**
 * Beangle tag Library
 *
 * @author chaostone
 * @since 2.0
 */
@description("beangle webmvc core 标签库")
class CoreTagLibrary extends AbstractTagLibrary {

  def getModels(req: HttpServletRequest, res: HttpServletResponse): AnyRef = {
    new CoreModels(buildComponentContext(req), req)
  }

}