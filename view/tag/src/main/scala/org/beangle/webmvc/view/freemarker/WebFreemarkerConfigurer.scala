package org.beangle.webmvc.view.freemarker

import java.io.{ File, IOException }

import org.beangle.commons.lang.Strings.{ split, substringAfter }
import org.beangle.commons.web.context.ServletContextHolder
import org.beangle.template.freemarker.{ BeangleClassTemplateLoader, FreemarkerConfigurer }

import freemarker.cache.{ FileTemplateLoader, MultiTemplateLoader, TemplateLoader, WebappTemplateLoader }
import freemarker.template.ObjectWrapper

class WebFreemarkerConfigurer extends FreemarkerConfigurer {

  override def createTemplateLoader(props: Map[String, String]): TemplateLoader = {
    templatePath = ServletContextHolder.context.getInitParameter("templatePath")
    val paths: Array[String] = split(templatePath, ",")
    val loaders = new collection.mutable.ListBuffer[TemplateLoader]
    for (path <- paths) {
      if (path.startsWith("class://")) {
        loaders += new BeangleClassTemplateLoader(substringAfter(path, "class://"))
      } else if (path.startsWith("file://")) {
        try {
          loaders += new FileTemplateLoader(new File(substringAfter(path, "file://")))
        } catch {
          case e: IOException =>
            throw new RuntimeException("templatePath: " + path + " cannot be accessed", e)
        }
      } else if (path.startsWith("webapp://")) {
        loaders += new WebappTemplateLoader(ServletContextHolder.context, substringAfter(path, "webapp://"))
      } else {
        throw new RuntimeException("templatePath: " + path
          + " is not well-formed. Use [class://|file://|webapp://] seperated with ,")
      }
    }
    new MultiTemplateLoader(loaders.toArray[TemplateLoader])
  }

  override def createObjectWrapper(props: Map[String, String]): ObjectWrapper = {
    val wrapper = new CachedObjectWrapper(true)
    wrapper.setUseCache(false)
    wrapper
  }

}