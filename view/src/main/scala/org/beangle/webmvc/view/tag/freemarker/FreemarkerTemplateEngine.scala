package org.beangle.webmvc.view.tag.freemarker

import java.io.{ IOException, Writer }
import java.util.{ ArrayList, HashMap }
import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.{ ClassLoaders, Throwables }
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.logging.Logging
import org.beangle.template.freemarker.BeangleClassTemplateLoader
import org.beangle.webmvc.api.context.ContextHolder
import org.beangle.webmvc.view.TagLibraryProvider
import org.beangle.webmvc.view.tag.{ Component, TemplateEngine }
import freemarker.cache.StrongCacheStorage
import freemarker.core.ParseException
import freemarker.ext.servlet.HttpRequestParametersHashModel
import freemarker.template.{ Configuration, ObjectWrapper, SimpleHash, Template, TemplateModel }
import javax.servlet.http.HttpServletRequest
import org.beangle.webmvc.view.freemarker.CachedObjectWrapper
import org.beangle.webmvc.view.freemarker.FreemarkerManager

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
class FreemarkerTemplateEngine(freemarkerManager: FreemarkerManager) extends TemplateEngine with Initializing with Logging {

  val config = new Configuration(Configuration.VERSION_2_3_23)

  var enableCache: Boolean = true

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component) = {
    val context = ContextHolder.context
    val model = freemarkerManager.createModel(config.getObjectWrapper, context.request, context.response, context)
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
    val wrapper = new CachedObjectWrapper(true)
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
