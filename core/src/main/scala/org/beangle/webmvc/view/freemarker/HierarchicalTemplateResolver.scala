package org.beangle.webmvc.view.freemarker

import java.io.{ FileNotFoundException, IOException }

import org.beangle.commons.lang.annotation.spi
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.view.ViewPathMapper
import org.beangle.webmvc.view.template.TemplateResolver

import freemarker.cache.TemplateLoader
import freemarker.template.Configuration

/**
 * Find template in class hierarchy with configuration without caching.
 */
class HierarchicalTemplateResolverByConfig(configuration: Configuration, viewMapper: ViewPathMapper, configurer: Configurer) extends TemplateResolver {

  def find(actionClass: Class[_], viewName: String, suffix: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    val profile = configurer.getProfile(actionClass.getName)
    do {
      val buf = new StringBuilder
      buf.append(viewMapper.map(superClass.getName, viewName, profile))
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
class HierarchicalTemplateResolverByLoader(loader: TemplateLoader, viewMapper: ViewPathMapper, configurer: Configurer) extends TemplateResolver {

  private val missings = new collection.mutable.HashSet[String]

  def find(actionClass: Class[_], viewName: String, suffix: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    var breakWhile = false
    val profile = configurer.getProfile(actionClass.getName)
    do {
      val buf = new StringBuilder
      buf.append(viewMapper.map(superClass.getName, viewName, profile))
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