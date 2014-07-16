package org.beangle.webmvc.route

import org.beangle.commons.lang.functor.Predicate
import org.beangle.commons.inject.Container

object ActionFinder {
  /**
   * Test whether the class is a action class.
   * <ul>
   * <li>Ends with suffix</li>
   * <li>In one of given packages</li>
   * </ul>
   */
  class Test(suffix: String, packages: Iterable[String]) extends Predicate[String] {
    def apply(name: String): Boolean = {
      if (name.endsWith(suffix)) {
        val classPackageName = if (name.indexOf(".") > 0) name.substring(0, name.lastIndexOf(".")) else ""
        packages.exists(packageName => Profile.isInPackage(packageName, classPackageName))
      } else {
        false
      }
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
        if (null != clazzType && actionTest.apply(clazzType.getName())) actions.put(clazzType, name.toString())
      }
    }
    actions.toMap
  }
}