/*
 * Beangle, Agile Development Scaffold and Toolkits.
 *
 * Copyright © 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
              logger.error("invoke set property [" + property + "] with value " + unwrapped, e)
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
