package org.beangle.webmvc.config.action

import org.beangle.webmvc.api.action.ActionSupport
import org.beangle.commons.io.ResourcePatternResolver
import org.beangle.commons.io.IOs
import org.beangle.commons.lang.annotation.description

@description("项目依赖库查看器")
class DependencyAction extends ActionSupport {

  def index(): String = {
    val resolver = new ResourcePatternResolver
    val urls = resolver.getResources("classpath*:META-INF/maven/**/pom.properties")
    val poms = new collection.mutable.ListBuffer[Map[String, String]]
    urls foreach { url =>
      poms += IOs.readJavaProperties(url)
    }
    put("jarPoms", poms.toList)
    forward()
  }
}