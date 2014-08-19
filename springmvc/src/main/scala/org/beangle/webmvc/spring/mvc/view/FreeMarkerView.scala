package org.beangle.webmvc.spring.mvc.view

import java.{ util => ju }

import scala.collection.JavaConversions.mapAsScalaMap

import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.tag.TagLibrary
import org.springframework.context.ApplicationContext

import freemarker.template.{ SimpleHash, Template }
import javax.servlet.ServletContext
import javax.servlet.http.{ HttpServletRequest, HttpServletResponse }

class FreeMarkerView extends org.springframework.web.servlet.view.freemarker.FreeMarkerView with Logging {

  var tags: Map[String, TagLibrary] = Map.empty

  var charsetPostfix: String = _

  protected override def initServletContext(servletContext: ServletContext): Unit = {
    super.initServletContext(servletContext)
    val names = servletContext.getAttributeNames()
    var wac: ApplicationContext = null
    while (names.hasMoreElements && null == wac) {
      val name = names.nextElement()
      servletContext.getAttribute(name) match {
        case ac: ApplicationContext => if (null != ac.getParent) wac = ac
        case _ =>
      }
    }
    if (null == wac) {
      warn("Cannot find web application context in servlet context!")
    } else {
      import scala.collection.JavaConversions._
      tags = wac.getBeansOfType(classOf[TagLibrary]).toMap
    }
    charsetPostfix = "; charset=" + getConfiguration.getDefaultEncoding
  }

  protected override def buildTemplateModel(model: ju.Map[String, Object], req: HttpServletRequest, res: HttpServletResponse): SimpleHash = {
    val data = super.buildTemplateModel(model, req, res)
    for ((tagName, tag) <- tags) {
      data.put(tagName, tag.getModels(req, res))
    }
    data
  }

  protected override def processTemplate(template: Template, model: SimpleHash, response: HttpServletResponse): Unit = {
    val attrContentType = template.getCustomAttribute("content_type")
    if (attrContentType != null) {
      response.setContentType(attrContentType.toString)
    } else {
      response.setContentType("text/html" + charsetPostfix)
    }
    template.process(model, response.getWriter())
  }
}