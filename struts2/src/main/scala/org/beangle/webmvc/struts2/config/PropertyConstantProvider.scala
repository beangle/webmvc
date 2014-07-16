package org.beangle.webmvc.struts2.config

import org.beangle.commons.logging.Logging

import com.opensymphony.xwork2.config.{Configuration, ConfigurationException, ConfigurationProvider}
import com.opensymphony.xwork2.inject.ContainerBuilder
import com.opensymphony.xwork2.util.location.LocatableProperties

class PropertyConstantProvider extends ConfigurationProvider with Logging {
  
  override def destroy(): Unit = {}
  
  @throws(classOf[ConfigurationException])
  override def init(configuration:Configuration):Unit = {}

  override def needsReload()  = false

  @throws(classOf[ConfigurationException])
  override def register(builder:ContainerBuilder, props:LocatableProperties):Unit = {
    val properties = System.getProperties()
    val keys = properties.propertyNames()
    while (keys.hasMoreElements()) {
      val key:String = keys.nextElement().asInstanceOf[String]
      if (null != props.getProperty(key)) {
        val value = properties.getProperty(key)
        props.setProperty(key, value, null)
        info(s"Override struts property ${key}=${value}")
      }
    }
  }

  @throws(classOf[ConfigurationException])
  override def loadPackages():Unit = {}
}