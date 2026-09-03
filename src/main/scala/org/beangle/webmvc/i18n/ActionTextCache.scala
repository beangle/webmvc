/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.i18n

import org.beangle.commons.lang.reflect.Invokers

import java.lang.invoke.{MethodHandles, VarHandle}

/** 进程级 action 文本缓存：volatile 字段 + VarHandle CAS。
 *  读多写少：读路径无锁（volatile load），写路径 CAS 重试合并，无 monitor（虚拟线程友好）。
 *  键/值 intern 由调用方通过 common 决定（公共默认文本才 intern 复用）。
 */
class ActionTextCache {
  @volatile private var caches: Map[Class[_], Map[String, String]] = Map.empty

  private val CACHES: VarHandle =
    Invokers.findVarHandle(MethodHandles.lookup(), classOf[ActionTextCache], "caches", classOf[Map[Class[_], Map[String, String]]])

  def getText(clazz: Class[_], key: String): Option[String] = {
    caches.get(clazz) match
      case None => None
      case Some(kvs) => kvs.get(key)
  }

  def update(clazz: Class[_], key: String, value: String, common: Boolean): Unit = {
    val kv = if common then (key.intern(), value.intern()) else (key, value)
    var done = false
    while (!done) {
      val old = caches
      val merged = old.get(clazz) match
        case Some(kvs) => old + (clazz -> (kvs + kv))
        case None => old + (clazz -> Map(kv))
      done = CACHES.compareAndSet(this, old, merged)
    }
  }
}
