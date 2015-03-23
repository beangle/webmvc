package org.beangle.webmvc.view.tag.freemarker

import java.io.Writer
import java.lang.reflect.Constructor
import java.{util => ju}

import org.beangle.commons.bean.Properties
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.tag.{Component, ComponentContext}

import freemarker.ext.beans.BeansWrapper
import freemarker.template.{TemplateModel, TemplateTransformModel}

class TagModel(context: ComponentContext, clazz: Class[_ <: Component] = null) extends TemplateTransformModel with Logging {

  private val componentCon: Constructor[_ <: Component] = if (clazz != null) clazz.getConstructor(classOf[ComponentContext]) else null

  private val wrapper = context.templateEngine.asInstanceOf[FreemarkerTemplateEngine].config.getObjectWrapper.asInstanceOf[BeansWrapper]
  
  def getWriter(writer: Writer, params: ju.Map[_, _]): Writer = {
    val bean = getBean()
    val iterator = params.keySet().iterator()
    while (iterator.hasNext()) {
      val key = iterator.next().asInstanceOf[String]
      val property = if (key == "class") "cssClass" else key
      val value = params.get(key).asInstanceOf[Object]
      if (value != null) {
        if (Properties.isWriteable(bean, property)) {
          val unwrapped = value match {
            case tm: TemplateModel => wrapper.unwrap(tm)
            case _ => value
          }
          try {
            Properties.set(bean, property, unwrapped)
          } catch {
            case e: Exception =>
              error("invoke set property [" + property + "] with value " + unwrapped, e)
          }
        } else {
          bean.parameters.put(key, value)
        }
      }
    }
    return new ResetCallbackWriter(bean, writer)
  }

  protected def getBean(): Component = {
    try {
      return componentCon.newInstance(context)
    } catch {
      case e: Exception =>
        throw new RuntimeException(e)
    }
  }
}
