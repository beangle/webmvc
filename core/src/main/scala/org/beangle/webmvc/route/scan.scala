package org.beangle.webmvc.route

import org.beangle.commons.lang.functor.Predicate
import org.beangle.commons.inject.Container

object ActionFinder {
  /**
   * Test whether the class is a action class.
   * <ul>
   * <li>Ends with suffix</li>
   * <li>In one of given profiles</li>
   * </ul>
   */
  class Test(suffix: String, routeService: RouteService) extends Predicate[Class[_]] {
    def apply(clazz: Class[_]): Boolean = {
      val className = clazz.getName
      if (className.endsWith(suffix)) {
        val profile = routeService.getProfile(className)
        null != profile && profile.actionScan
      } else false
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
    if (null != container) {
      container.keys() foreach { name =>
        val clazzType = container.getType(name).orNull
        if (null != clazzType && actionTest.apply(clazzType)) actions.put(clazzType, name.toString)
      }
    }
    actions.toMap
  }
}