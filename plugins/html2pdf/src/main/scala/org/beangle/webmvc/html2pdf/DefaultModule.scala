package org.beangle.webmvc.html2pdf

import org.beangle.commons.inject.bind.AbstractBindModule

/**
 * @author chaostone
 */
class DefaultModule extends AbstractBindModule {

  override def binding(): Unit = {
    bind("web.Serializer.pdf", classOf[PdfSerializer])
  }
}