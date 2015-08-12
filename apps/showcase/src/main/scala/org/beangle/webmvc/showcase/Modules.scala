package org.beangle.webmvc.showcase

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.showcase.action.{ ParamAction, SerialAction, StatusAction, StreamAction }
import org.beangle.webmvc.showcase.action.PersonAction

object DefaultModule extends AbstractBindModule {

  protected def binding(): Unit = {
    bind(classOf[StreamAction], classOf[StatusAction], classOf[SerialAction], classOf[ParamAction])
    bind(classOf[PersonAction])
  }
}