/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2016, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.view.impl

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