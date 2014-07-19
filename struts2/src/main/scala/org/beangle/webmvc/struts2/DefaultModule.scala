package org.beangle.webmvc.struts2

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.view.freemarker.FreemarkerTemplateEngine
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.apache.struts2.StrutsConstants
import org.beangle.commons.inject.PropertySource
import org.beangle.commons.lang.ClassLoaders
import java.net.URL
import org.beangle.commons.io.IOs

class DefaultModule extends AbstractBindModule with PropertySource {

  protected override def binding(): Unit = {
    bind(classOf[FreemarkerTemplateEngine])
    bind("b", classOf[BeangleTagLibrary]).property("suffix", $(StrutsConstants.STRUTS_ACTION_EXTENSION))
  }

  override def properties: collection.Map[String, String] = {
    val constants = new collection.mutable.HashMap[String, String]
    //1. read default.properties
    constants ++= IOs.readJavaProperties(ClassLoaders.getResource("org/apache/struts2/default.properties", getClass).openStream())

    //2. read struts-plugin.xml
    ClassLoaders.getResources("struts-plugin.xml", getClass) foreach { url =>
      filterContants(url, constants)
    }

    //3. read struts.xml and struts.properties
    filterContants(ClassLoaders.getResource("struts.xml", getClass), constants)
    val finalProperties = ClassLoaders.getResource("struts.properties", getClass)
    if (null != finalProperties)
      constants ++= IOs.readJavaProperties(finalProperties.openStream())
    constants
  }

  private def filterContants(url: URL, constants: collection.mutable.HashMap[String, String]): Unit = {
    if (null == url) return
    val is = url.openStream
    scala.xml.XML.load(is) \\ "constant" foreach { constantElem =>
      constants.put((constantElem \ "@name").text, (constantElem \ "@value").text)
    }
    is.close()
  }
}