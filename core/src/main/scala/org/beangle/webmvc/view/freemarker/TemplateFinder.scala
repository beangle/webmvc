package org.beangle.webmvc.view.freemarker

import java.io.{ FileNotFoundException, IOException }

import org.beangle.webmvc.route.ViewMapper

import freemarker.cache.TemplateLoader
import freemarker.template.Configuration

/**
 * Try to find template
 *
 * @author chaostone
 */
trait TemplateFinder {
  def find(actionClass: Class[_], viewName: String, suffix: String): String
}

/**
 * Find template in class hierarchy with configuration without caching.
 */
class TemplateFinderByConfig(configuration: Configuration, viewMapper: ViewMapper) extends TemplateFinder {

  def find(actionClass: Class[_], viewName: String, suffix: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    do {
      val buf = new StringBuilder
      buf.append(viewMapper.map(superClass.getName, viewName))
      buf.append(suffix)
      path = buf.toString
      var templateName = path
      if (path.charAt(0) != '/') templateName = "/" + templateName
      try {
        source = configuration.getTemplate(templateName)
      } catch {
        case e: FileNotFoundException => null //ignore
        case e: IOException => {
          source = "error ftl"
        }
      }
      superClass = superClass.getSuperclass
    } while (null == source && !superClass.equals(classOf[Object]) && !superClass.isPrimitive)
    if (null == source) null else path
  }
}

/**
 * Try find template in class hierarchy,caching missing result.
 */
class TemplateFinderByLoader(val loader: TemplateLoader, val viewMapper: ViewMapper) extends TemplateFinder {

  private val missings = new collection.mutable.HashSet[String]

  def find(actionClass: Class[_], viewName: String, suffix: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    var breakWhile = false
    do {
      val buf = new StringBuilder
      buf.append(viewMapper.map(superClass.getName, viewName))
      buf.append(suffix)
      path = buf.toString
      var templateName = path
      //different with config finder
      if (path.charAt(0) == '/') templateName = templateName.substring(1)
      if (!missings.contains(templateName)) {
        try {
          source = loader.findTemplateSource(templateName)
          if (null != source) loader.closeTemplateSource(source)
          else missings.add(templateName)
        } catch {
          case e: IOException =>
            breakWhile = true
        }
      }
      if (!breakWhile) superClass = superClass.getSuperclass
    } while (!breakWhile && null == source && !superClass.equals(classOf[Object]) && !superClass.isPrimitive)
    if (null == source) null else path
  }
}