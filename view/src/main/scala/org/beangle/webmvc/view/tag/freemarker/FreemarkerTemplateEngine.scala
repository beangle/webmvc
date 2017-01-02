/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2017, Beangle Software.
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
package org.beangle.webmvc.view.tag.freemarker

import java.io.{ IOException, Writer }
import java.util.{ ArrayList, HashMap }

import org.beangle.commons.bean.Initializing
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Throwables }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.commons.template.freemarker.BeangleClassTemplateLoader
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.view.freemarker.{ CachedObjectWrapper, FreemarkerModelBuilder }
import org.beangle.webmvc.view.tag.{ Component, TemplateEngine }

import freemarker.cache.StrongCacheStorage
import freemarker.core.ParseException
import freemarker.template.{ Configuration, Template }

/**
 * Freemarker Template Engine
 * <ul>
 * <li>User hashmodel store in request</li>
 * <li>Load hierarchical templates</li>
 * <li>Disabled freemarker localized lookup in template loading</li>
 * </ul>
 *
 * @author chaostone
 */
@description("Freemarker 模板引擎")
class FreemarkerTemplateEngine(modelBuilder: FreemarkerModelBuilder) extends TemplateEngine with Initializing with Logging {

  val config = new Configuration(Configuration.VERSION_2_3_24)

  var enableCache: Boolean = true

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component) = {
    val context = ActionContext.current
    val model = modelBuilder.createModel(config.getObjectWrapper, context.request, context.response, context)
    val prevTag = model.get("tag")
    model.put("tag", component)
    getTemplate(template).process(model, writer)
    if (null != prevTag) model.put("tag", prevTag)
  }

  /**
   * Clone configuration from FreemarkerManager,but custmize in
   * <ul>
   * <li>Disable freemarker localized lookup
   * <li>Using tag.properties
   * <li>Disable auto imports and includes
   * </ul>
   */
  override def init(): Unit = {
    config.setEncoding(config.getLocale(), "UTF-8")
    IOs.readJavaProperties(ClassLoaders.getResource("org/beangle/webmvc/view/tag/freemarker/tag.properties")) foreach {
      case (k, v) => config.setSetting(k, v)
    }
    val wrapper = new CachedObjectWrapper()
    wrapper.setUseCache(false)
    config.setObjectWrapper(wrapper)
    config.setTemplateLoader(new HierarchicalTemplateLoader(new BeangleClassTemplateLoader()))

    if (!enableCache) config.setTemplateUpdateDelayMilliseconds(0)

    config.setCacheStorage(new StrongCacheStorage())
    // Disable auto imports and includes
    config.setAutoImports(new HashMap(0))
    config.setAutoIncludes(new ArrayList(0))
  }

  /**
   * Load template in hierarchical path
   */
  private def getTemplate(templateName: String): Template = {
    try {
      return config.getTemplate(templateName, "UTF-8")
    } catch {
      case e: ParseException => throw e
      case e: IOException =>
        logger.error(s"Couldn't load template '${templateName}',loader is ${config.getTemplateLoader().getClass()}")
        throw Throwables.propagate(e)
    }
  }

  final def suffix = ".ftl"
}
