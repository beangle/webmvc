package org.beangle.webmvc.context.impl

import org.beangle.commons.lang.functor.Predicate
import org.beangle.commons.inject.Container
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.context.ActionFinder

/**
 * Find actions from application context
 */
class ContainerActionFinder(val container: Container) extends ActionFinder {

  def getActions(actionTest: ActionFinder.Test): Seq[Object] = {
    val actions = new collection.mutable.ListBuffer[Object]
    container.keys() foreach { name =>
      val bean:Object = container.getBean(name).get
      if (actionTest.apply(bean.getClass)) actions += bean
    }
    actions
  }
}