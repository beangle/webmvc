/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.config.impl

import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.intercept.Interceptor
import org.beangle.webmvc.config.{ ActionMapping, RouteMapping, ActionMappingBuilder, Configurer, Profile, ProfileProvider }
import org.beangle.webmvc.context.ActionFinder

@description("缺省配置器")
class DefaultConfigurer(profileProvider: ProfileProvider, container: Container) extends Configurer with Logging {

  private val class2Profiles = new collection.mutable.HashMap[String, Profile]

  var actionMappings: Map[String, ActionMapping] = Map.empty
  var classMappings: Map[Class[_], ActionMapping] = Map.empty
  var profiles: List[Profile] = Nil

  var actionMappingBuilder: ActionMappingBuilder = _

  var actionFinder: ActionFinder = _

  override def build(): Unit = {
    val watch = new Stopwatch(true)
    profiles = profileProvider.loadProfiles() map { pc =>
      val interceptors = pc.interceptorNames map { interName =>
        container.getBean[Interceptor](interName).get
      }
      pc.mkProfile(interceptors)
    }
    profiles = profiles.sorted

    var actionCount, mappingCount = 0
    val mutableActionMappings = new collection.mutable.HashMap[String, ActionMapping]
    val mutableClassMappings = new collection.mutable.HashMap[Class[_], ActionMapping]
    actionFinder.getActions(new ActionFinder.Test(this)) foreach { bean =>
      val clazz = bean.getClass
      val mapping = actionMappingBuilder.build(bean, clazz, this.getProfile(clazz.getName))
      if (!mapping.mappings.isEmpty) {
        mutableClassMappings.put(mapping.clazz, mapping)
        mutableActionMappings.put(mapping.name, mapping)
        actionCount += 1
        mappingCount += mapping.mappings.size
      }
    }
    actionMappings = mutableActionMappings.toMap
    classMappings = mutableClassMappings.toMap
    logger.info(s"Action scan completed,create $actionCount actions($mappingCount mappings) in ${watch}.")
  }

  override def getProfile(className: String): Profile = {
    var matched = class2Profiles.get(className).orNull
    if (null != matched) return matched
    profiles.find(p => !p.matches(className).isEmpty) match {
      case Some(p) =>
        class2Profiles.put(className, p)
        matched = p
        logger.debug(s"${className} match profile:${p}")
      case None =>
    }
    matched
  }

  override def getRouteMapping(clazz: Class[_], method: String): Option[RouteMapping] = {
    classMappings.get(clazz) match {
      case Some(am) => am.mappings.get(method)
      case None     => None
    }
  }

  override def getActionMapping(name: String): Option[ActionMapping] = {
    actionMappings.get(name)
  }
}
