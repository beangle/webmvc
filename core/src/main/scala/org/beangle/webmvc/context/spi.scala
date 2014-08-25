package org.beangle.webmvc.context

import java.{ util => ju }

import org.beangle.commons.lang.annotation.spi
import org.beangle.commons.lang.functor.Predicate
import org.beangle.webmvc.config.Configurer

import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }
@spi
trait LocaleResolver {
  def resolve(request: HttpServletRequest): ju.Locale
  def setLocale(request: HttpServletRequest, response: HttpServletResponse, locale: ju.Locale): Unit
}

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
@spi
trait ActionFinder {

  def getActions(test: ActionFinder.Test): Seq[Object]

}