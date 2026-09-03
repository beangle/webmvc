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

import org.beangle.commons.aot.{AotHintRegistrar, AotPolicy}

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
    hints.registerType(classOf[org.beangle.webmvc.config.ProfileConfig])
    // 模板模型类
    hints.registerType(
      classOf[org.beangle.webmvc.view.tag.CoreModels],
      classOf[org.beangle.webmvc.asset.Static])
    // 注解族
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
      classOf[org.beangle.webmvc.annotation.views])

    // 配置与映射
    hints.registerType(
      classOf[org.beangle.webmvc.config.ActionMapping],
      classOf[org.beangle.webmvc.config.ActionMappingBuilder],
      classOf[org.beangle.webmvc.config.Buildable],
      classOf[org.beangle.webmvc.config.Configurator],
      classOf[org.beangle.webmvc.config.Profile],
      classOf[org.beangle.webmvc.config.ProfileProvider])

    // 请求调度
    hints.registerType(
      classOf[org.beangle.webmvc.dispatch.AbstractExceptionHandler],
      classOf[org.beangle.webmvc.dispatch.ActionUriRender],
      classOf[org.beangle.webmvc.dispatch.ExceptionHandler],
      classOf[org.beangle.webmvc.dispatch.RequestMapper],
      classOf[org.beangle.webmvc.dispatch.Route],
      classOf[org.beangle.webmvc.dispatch.RouteProvider])

    // 执行与拦截
    hints.registerType(
      classOf[org.beangle.webmvc.execution.InvokerBuilder],
      classOf[org.beangle.webmvc.execution.ResponseCache])

    // 视图
    hints.registerType(
      classOf[org.beangle.webmvc.view.TemplatePathMapper],
      classOf[org.beangle.webmvc.view.TemplateResolver],
      classOf[org.beangle.webmvc.view.TypeViewBuilder],
      classOf[org.beangle.webmvc.view.ViewBuilder],
      classOf[org.beangle.webmvc.view.ViewManager],
      classOf[org.beangle.webmvc.view.ViewRender],
      classOf[org.beangle.webmvc.view.ViewResolver])

    // 支持类
    hints.registerType(
      classOf[org.beangle.webmvc.support.ActionSupport],
      classOf[org.beangle.webmvc.support.EntitySupport],
      classOf[org.beangle.webmvc.support.MessageSupport],
      classOf[org.beangle.webmvc.support.ParamSupport],
      classOf[org.beangle.webmvc.support.RouteSupport],
      classOf[org.beangle.webmvc.support.ServletSupport])

    // i18n
    hints.registerType(
      classOf[org.beangle.webmvc.i18n.ActionTextCache],
      classOf[org.beangle.webmvc.i18n.TextResourceProvider])

    // 上下文
    hints.registerType(
      classOf[org.beangle.webmvc.context.ActionContextProperty])

    // 资源
    hints.registerType(
      classOf[org.beangle.webmvc.asset.StaticFactory])
  }
}
