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

import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.util.concurrent.{CountDownLatch, Executors, TimeUnit}

class ActionTextCacheTest extends AnyFunSpec with Matchers {

  describe("ActionTextCache") {
    it("get and update") {
      val cache = new ActionTextCache
      assert(cache.getText(classOf[String], "k").isEmpty)
      cache.update(classOf[String], "k", "v", false)
      assert(cache.getText(classOf[String], "k") == Some("v"))
      cache.update(classOf[String], "k", "v2", false)
      assert(cache.getText(classOf[String], "k") == Some("v2"))
    }

    it("intern common text") {
      val cache = new ActionTextCache
      cache.update(classOf[String], new String("k"), new String("v"), true)
      assert(cache.getText(classOf[String], "k").get eq "v")
    }

    it("concurrent updates are not lost") {
      val cache = new ActionTextCache
      val threads = 8
      val perThread = 500
      val pool = Executors.newFixedThreadPool(threads)
      val latch = new CountDownLatch(1)
      try {
        (0 until threads).foreach { t =>
          pool.execute(() => {
            latch.await()
            (0 until perThread).foreach { i =>
              cache.update(classOf[String], s"k$t-$i", s"v$t-$i", false)
            }
          })
        }
        latch.countDown()
        pool.shutdown()
        assert(pool.awaitTermination(10, TimeUnit.SECONDS))
      } finally pool.shutdownNow()

      (0 until threads).foreach { t =>
        (0 until perThread).foreach { i =>
          assert(cache.getText(classOf[String], s"k$t-$i") == Some(s"v$t-$i"))
        }
      }
    }
  }
}
