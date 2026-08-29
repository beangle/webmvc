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

package org.beangle.webmvc.aot

import org.beangle.commons.aot.AotHintRegistrar

/** webmvc 的 GraalVM native-image 反射/资源提示。
  *
  * 注册 webmvc 库自身的反射面与资源：
  *  - 注解族：Action 扫描与映射经 `getAnnotation` 反射读取
  *    （`mapping`/`action`/`cache`/`response`/`ignore`/`views`/`param`/`cookie`/`header`/`body`/`view`）；
  *
  * `description` 注解与 `beangle.xml` 资源属 commons 机制面（cdi `Binder`/`XmlConfigs`），
  * SPI 文件（`META-INF/services/` 下文件）与 i18n bundle（`*.zh_CN`）亦归 commons 机制面，
  * 均已在 commons 的 `BuiltinAotHints` 集中注册，此处不重复。
  * `*.ftl`（freemarker 模板）属 template 机制面，已在 template 的 `TemplateAotHints`
  * 集中注册，此处不重复。
  *
  * cdi 模块（`DefaultModule`/`ViewModule`/`DevModule`）与绑定类由构建期 `MetaPlugin`/
  * `AotPlugin` 依据 `beangle.xml` 自动注册，此处不重复声明。
  */
class WebmvcAotHints extends AotHintRegistrar {

  override def registering(): Unit = {
    hints.registerType(
      classOf[org.beangle.webmvc.annotation.action],
      classOf[org.beangle.webmvc.annotation.body],
      classOf[org.beangle.webmvc.annotation.cache],
      classOf[org.beangle.webmvc.annotation.cookie],
      classOf[org.beangle.webmvc.annotation.header],
      classOf[org.beangle.webmvc.annotation.ignore],
      classOf[org.beangle.webmvc.annotation.mapping],
      classOf[org.beangle.webmvc.annotation.param],
      classOf[org.beangle.webmvc.annotation.response],
      classOf[org.beangle.webmvc.annotation.view],
      classOf[org.beangle.webmvc.annotation.views]
    )
  }
}
