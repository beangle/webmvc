package org.beangle.webmvc.hibernate

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.hibernate.action.{ ConfigAction, StatAction }
import org.beangle.webmvc.hibernate.helper.SessionFactoryHelper
import org.beangle.webmvc.hibernate.action.IndexAction
import org.beangle.webmvc.hibernate.interceptor.OpenSessionInViewInterceptor

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    bind(classOf[IndexAction], classOf[StatAction], classOf[ConfigAction])
    bind(classOf[SessionFactoryHelper])
    bind("mvc.Interceptor.hibernate", classOf[OpenSessionInViewInterceptor])
  }
}