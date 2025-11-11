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

package org.beangle.webmvc.support.spring

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.joran.JoranConfigurator
import jakarta.servlet.ServletContext
import org.beangle.commons.cdi.BindRegistry
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{JVM, Strings}
import org.beangle.web.servlet.init.Initializer
import org.slf4j.LoggerFactory

/** 如果是开发环境且项目中logback-dev.xml文件则采用这个文件。
 */
class LoggerInitializer extends Initializer {

  override def onStartup(sc: ServletContext): Unit = {
    if (devEnabled) {
      if (null == System.getProperty("logback.configurationFile")) {
        val devFile = sc.getClassLoader.getResource("logback-dev.xml")

        if (null != devFile) {
          val is = devFile.openStream()
          try
            val lc = LoggerFactory.getILoggerFactory.asInstanceOf[LoggerContext]
            lc.reset()
            val configurator = new JoranConfigurator
            configurator.setContext(lc)
            configurator.doConfigure(is)
          catch {
            case e: Exception => throw new RuntimeException("重新加载 Logback 配置失败", e)
          } finally {
            IOs.close(is)
          }
        }
      }
    }
  }

  private final def devEnabled: Boolean = {
    val profiles = System.getProperty(BindRegistry.ProfileProperty)
    val enabled = null != profiles && Strings.split(profiles, ",").toSet.contains("dev")
    enabled || JVM.isDebugMode
  }

}
