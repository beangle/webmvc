package org.beangle.webmvc.view.freemarker

import java.io.{FileNotFoundException, IOException}

import org.beangle.webmvc.route.ViewMapper

import freemarker.cache.TemplateLoader
import freemarker.template.Configuration


/**
 * Try to find template
 *
 * @author chaostone
 */
trait TemplateFinder {
  def find(actionClass: Class[_], method: String, viewName: String, extention: String): String

}

class TemplateFinderByConfig extends TemplateFinder {

  private var configuration: Configuration = _

  private var viewMapper: ViewMapper = _

  def this(configuration: Configuration, viewMapper: ViewMapper) = {
    this()
    this.configuration = configuration
    this.viewMapper = viewMapper
  }

  def find(actionClass: Class[_], method: String, viewName: String, extention: String): String = {
    var path: String = null
    var superClass = actionClass
    var source: Object = null
    do {
      val buf = new StringBuilder()
      buf.append(viewMapper.getViewPath(superClass.getName(), method, viewName))
      buf.append('.').append(extention)
      path = buf.toString()
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
      superClass = superClass.getSuperclass()
    } while (null == source && !superClass.equals(classOf[Object]) && !superClass.isPrimitive())
    if (null == source) null else path
  }
}


class TemplateFinderByLoader(val loader:TemplateLoader, val viewMapper:ViewMapper ) extends TemplateFinder{

  private val missings = new collection.mutable.HashSet[String]()

  def find(actionClass:Class[_], method:String, viewName:String, extention:String):String = {
    var path:String = null
    var superClass:Class[_] = actionClass
    var source:Object = null
    var breakWhile = false
    do {
      val buf = new StringBuilder()
      buf.append(viewMapper.getViewPath(superClass.getName(), method, viewName))
      buf.append('.').append(extention)
      path = buf.toString()
      var templateName = path
      if (path.charAt(0) == '/') templateName = templateName.substring(1)
      if (!missings.contains(templateName.asInstanceOf)) {
        try {
          source = loader.findTemplateSource(templateName)
          if (null != source) loader.closeTemplateSource(source)
          else missings.add(templateName)
        } catch{
          case e:IOException =>
          breakWhile = true
        }
      }
      if(!breakWhile) superClass = superClass.getSuperclass()
    } while (!breakWhile && null == source && !superClass.equals(classOf[Object]) && !superClass.isPrimitive())
    if(null == source) null else path
  }
}