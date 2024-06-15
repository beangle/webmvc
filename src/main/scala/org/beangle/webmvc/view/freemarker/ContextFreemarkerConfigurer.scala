/*
 * Copyright (C) 2005, The Beangle Software.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.beangle.webmvc.view.freemarker

import freemarker.cache.{MultiTemplateLoader, TemplateLoader}
import freemarker.template.ObjectWrapper
import org.beangle.commons.lang.Strings.{split, substringAfter}
import org.beangle.data.orm.hibernate.HibernateProxyResolver
import org.beangle.template.freemarker.{Configurer, ProfileTemplateLoader}
import org.beangle.web.servlet.context.ServletContextHolder

/**
 * @author chaostone
 */
class ContextFreemarkerConfigurer extends Configurer {

  override def createObjectWrapper(props: Map[String, String]): ObjectWrapper = {
    val wrapper = new ContextObjectWrapper()
    wrapper.setUseCache(false)
    //FIXME hard code,but Jpas.proxyResolver is so late for initialization.
    wrapper.proxyResolver = HibernateProxyResolver
    wrapper
  }

  protected override def detectTemplatePath(props: Map[String, String]): String = {
    if null == templatePath then templatePath = ServletContextHolder.context.getInitParameter("templatePath")
    if null == templatePath then templatePath = props.getOrElse("template_path", "class://")
    if null == templatePath then templatePath = "class://"
    templatePath
  }

  override def createTemplateLoader(props: Map[String, String]): TemplateLoader = {
    val paths = split(detectTemplatePath(props), ",")
    val loaders = new collection.mutable.ListBuffer[TemplateLoader]
    for (path <- paths) {
      if (path.startsWith("webapp://")) {
        loaders += new WebappTemplateLoader(ServletContextHolder.context, substringAfter(path, "webapp://"))
      } else {
        loaders += super.buildLoader(path)
      }
    }
    ProfileTemplateLoader(MultiTemplateLoader(loaders.toArray[TemplateLoader]))
  }
}
