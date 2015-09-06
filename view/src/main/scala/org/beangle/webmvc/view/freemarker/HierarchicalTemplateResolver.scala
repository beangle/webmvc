package org.beangle.webmvc.view.freemarker

import java.io.{ FileNotFoundException, IOException }

import org.beangle.commons.lang.annotation.description
import org.beangle.template.freemarker.FreemarkerConfigurer
import org.beangle.webmvc.config.Configurer
import org.beangle.webmvc.view.{ TemplatePathMapper, TemplateResolver }

/**
 * Find template in class hierarchy with configuration without caching.
 * It need a ViewPathMapper
 */
@description("参考类层级模板查找器")
class HierarchicalTemplateResolver(freemarkerConfigurer: FreemarkerConfigurer, templatePathMapper: TemplatePathMapper, configurer: Configurer) extends TemplateResolver {

  override def resolve(actionClass: Class[_], viewName: String, suffix: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    val profile = configurer.getProfile(actionClass.getName)
    val configuration = freemarkerConfigurer.config
    do {
      val buf = new StringBuilder
      buf.append(templatePathMapper.map(superClass.getName, viewName, profile))
      buf.append(suffix)
      path = buf.toString
      try {
        source = configuration.getTemplate(path)
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