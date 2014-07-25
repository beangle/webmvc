package org.beangle.webmvc.view.freemarker

import java.io.{ IOException, Writer }
import java.util.{ ArrayList, HashMap }

import org.beangle.commons.bean.Initializing
import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Throwables
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.context.ContextHolder
import org.beangle.webmvc.view.component.Component
import org.beangle.webmvc.view.tag.TagLibrary
import org.beangle.webmvc.view.template.AbstractTemplateEngine

import freemarker.cache.StrongCacheStorage
import freemarker.core.ParseException
import freemarker.ext.servlet.HttpRequestParametersHashModel
import freemarker.template.{ Configuration, ObjectWrapper, SimpleHash, Template, TemplateModel }
import javax.servlet.http.HttpServletRequest

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
class FreemarkerTemplateEngine extends AbstractTemplateEngine with Logging with Initializing {

  protected val config = buildConfig()
  private val templateModelAttribute = ".freemarker.TemplateModel"
  protected var tagLibraries: Map[String, TagLibrary] = Map.empty
  protected var container: Container = _

  @throws(classOf[Exception])
  def render(template: String, writer: Writer, component: Component) = {
    val model = buildModel(component)
    val prevTag = model.get("tag")
    model.put("tag", component)
    getTemplate(template).process(model, writer)
    if (null != prevTag) model.put("tag", prevTag)
  }

  /**
   * Clone configuration from FreemarkerManager,but custmize in
   * <ul>
   * <li>Disable freemarker localized lookup
   * <li>Cache two hour(7200s) and Strong cache
   * <li>Disable auto imports and includes
   * </ul>
   */
  def buildConfig(): Configuration = {
    val config = new Configuration()
    config.setTemplateLoader(new HierarchicalTemplateLoader(new BeangleClassTemplateLoader()))
    // Disable freemarker localized lookup
    config.setLocalizedLookup(false)
    config.setEncoding(config.getLocale(), "UTF-8")

    config.setObjectWrapper(new BeangleObjectWrapper(false))
    // Cache one hour(7200s) and Strong cache
    config.setTemplateUpdateDelay(0)
    // config.setCacheStorage(new MruCacheStorage(100,250))
    config.setCacheStorage(new StrongCacheStorage())

    // Disable auto imports and includes
    config.setAutoImports(new HashMap(0))
    config.setAutoIncludes(new ArrayList(0))
    config
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
        error(s"Couldn't load template '${templateName}',loader is ${config.getTemplateLoader().getClass()}")
        throw Throwables.propagate(e)
    }
  }

  /**
   * componentless model(one per request)
   */
  private def buildModel(component: Component): SimpleHash = {
    val context = ContextHolder.context
    val req = context.request
    var model = req.getAttribute(templateModelAttribute).asInstanceOf[SimpleHash]
    if (null == model) {
      val model = new SimpleHttpScopesHashModel(config.getObjectWrapper(), req)
      model.put("Parameters", new HttpRequestParametersHashModel(req))
      val res = ContextHolder.context.response
      for ((k, v) <- tagLibraries) {
        model.put(k, v.getModels(req, res))
      }
      req.setAttribute(templateModelAttribute, model)
    }
    return model
  }

  final def suffix = ".ftl"

  def init(): Unit = {
    tagLibraries = container.getBeans(classOf[TagLibrary]).asInstanceOf[Map[String, TagLibrary]]
  }
}

/**
 * Just extract value from default scope and request(omit session/context)
 */
class SimpleHttpScopesHashModel(wrapper: ObjectWrapper, val request: HttpServletRequest) extends SimpleHash {

  setObjectWrapper(wrapper)

  override def get(key: String): TemplateModel = {
    // Lookup in page scope
    val model = super.get(key);
    if (model != null) {
      return model;
    }

    // Lookup in request scope
    val obj = request.getAttribute(key);
    if (obj != null) {
      return wrap(obj);
    }
    // return wrapper's null object (probably null).        
    return wrap(null);
  }
}