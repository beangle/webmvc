package org.beangle.webmvc.context

import org.beangle.commons.lang.functor.Predicate
import org.beangle.commons.inject.Container
import org.beangle.webmvc.config.Configurer

object ActionFinder {
  /**
   * Test whether the class is a action class.
   * <ul>
   * <li>Ends with suffix</li>
   * <li>In one of given profiles</li>
   * </ul>
   */
  class Test(configurer: Configurer) extends Predicate[Class[_]] {
    def apply(clazz: Class[_]): Boolean = {
      null != configurer.getProfile(clazz.getName)
    }
  }
}
/**
 * Find Action from ObjectFactory
 */
trait ActionFinder {

  def getActions(test: ActionFinder.Test): Map[Class[_], String]

}
/**
 * Find actions from application context
 */
class ContainerActionFinder(val container: Container) extends ActionFinder {

  def getActions(actionTest: ActionFinder.Test): Map[Class[_], String] = {
    val actions = new collection.mutable.HashMap[Class[_], String]()
    container.keys() foreach { name =>
      val clazzType = container.getType(name).orNull
      if (null != clazzType && actionTest.apply(clazzType)) actions.put(clazzType, name.toString)
    }
    actions.toMap
  }
}