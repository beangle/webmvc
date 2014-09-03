package org.beangle.webmvc.view.tag.freemarker

import java.io.Reader
import org.beangle.webmvc.view.tag.Themes
import freemarker.cache.TemplateLoader

class HierarchicalTemplateLoader(loader: TemplateLoader) extends TemplateLoader {

  def findTemplateSource(name: String): Object = {
    var resource = name
    var source = loader.findTemplateSource(resource)
    if (null == source) {
      val searched = new collection.mutable.HashSet[String]
      searched.add(resource)
      var break = false
      do {
        resource = Themes.getParentTemplate(resource)
        if (null == resource || searched.contains(resource)) break = true
        else {
          source = loader.findTemplateSource(resource)
          searched.add(resource)
        }
      } while (null == source && !break)
    }
    return source
  }

  def getLastModified(templateSource: Object): Long = {
    return loader.getLastModified(templateSource)
  }

  def getReader(templateSource: Object, encoding: String): Reader = {
    return loader.getReader(templateSource, encoding)
  }

  def closeTemplateSource(templateSource: Object) {
    loader.closeTemplateSource(templateSource)
  }
}