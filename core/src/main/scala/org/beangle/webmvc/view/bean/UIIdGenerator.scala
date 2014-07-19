package org.beangle.webmvc.view.bean

import scala.util.Random
import org.beangle.commons.lang.Strings

trait UIIdGenerator {

  def generate(clazz: Class[_]): String
}

class RandomIdGenerator extends UIIdGenerator {

  val seed = new Random()

  def generate(clazz: Class[_]): String = {
    val nextInt = {
      val next = seed.nextInt()
      if (next == Integer.MIN_VALUE) Integer.MAX_VALUE else Math.abs(next)
    }
    Strings.uncapitalize(clazz.getSimpleName()) + String.valueOf(nextInt)
  }

}

/**
 * 基于每种ui一个序列的id产生器
 *
 * @author chaostone
 * @since 3.0
 */
class IndexableIdGenerator(seed: String) extends UIIdGenerator {

  private val uiIndexes = new collection.mutable.HashMap[Class[_], UIIndex]

  def generate(clazz: Class[_]): String = {
    val index = uiIndexes.get(clazz).getOrElse({
      val index = new UIIndex(Strings.uncapitalize(clazz.getSimpleName()))
      uiIndexes.put(clazz, index)
      index
    })
    index.genId(seed)
  }
}

class UIIndex(name: String) {
  var index = 0
  def genId(seed: String): String = {
    index = index + 1
    Strings.concat(name, seed, String.valueOf(index))
  }
}