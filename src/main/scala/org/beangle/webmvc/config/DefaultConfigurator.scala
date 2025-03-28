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

package org.beangle.webmvc.config

import org.beangle.commons.cdi.Container
import org.beangle.commons.lang.annotation.description
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.webmvc.view.ViewDecorator
import org.beangle.web.servlet.intercept.Interceptor

@description("缺省配置器")
class DefaultConfigurator(profileProvider: ProfileProvider, container: Container) extends Configurator, Logging {

  private val class2Profiles = new collection.mutable.HashMap[String, Profile]

  var actionMappings: Map[String, ActionMapping] = Map.empty
  var classMappings: Map[Class[_], ActionMapping] = Map.empty
  var profiles: List[Profile] = Nil
  var actionMappingBuilder: ActionMappingBuilder = _

  override def build(): Unit = {
    val watch = new Stopwatch(true)
    profiles = profileProvider.loadProfiles() map { pc =>
      val interceptors = pc.interceptorNames map { name =>
        container.getBean[Interceptor](name) match {
          case Some(i) => i
          case None => throw new RuntimeException(s"Cannot find interceptor [$name] in container")
        }
      }
      val decorators = pc.decoratorNames map { name =>
        container.getBean[ViewDecorator](name) match {
          case Some(d) => d
          case None => throw new RuntimeException(s"Cannot find decorator [$name] in container")
        }
      }
      pc.mkProfile(interceptors, decorators)
    }
    profiles = profiles.sorted

    var actionCount, mappingCount = 0
    val mutableActionMappings = new collection.mutable.HashMap[String, ActionMapping]
    val mutableClassMappings = new collection.mutable.HashMap[Class[_], ActionMapping]
    val actionFinder = new ContainerActionFinder(container)
    actionFinder.actions(new ActionFinder.Test(this)) foreach { bean =>
      val clazz = bean.getClass
      val mapping = actionMappingBuilder.build(bean, clazz, this.getProfile(clazz.getName))
      if (mapping.mappings.nonEmpty) {
        mutableClassMappings.put(mapping.clazz, mapping)
        mutableActionMappings.put(mapping.name, mapping)
        actionCount += 1
        mappingCount += mapping.mappings.size
      }
    }
    actionMappings = mutableActionMappings.toMap
    classMappings = mutableClassMappings.toMap
    logger.info(s"Action scan completed,create $actionCount actions($mappingCount mappings) in $watch.")
  }

  override def getProfile(className: String): Profile = {
    var matched = class2Profiles.get(className).orNull
    if (null != matched) return matched
    profiles.find(p => p.matches(className).nonEmpty) match {
      case Some(p) =>
        class2Profiles.put(className, p)
        matched = p
        logger.debug(s"$className match profile:$p")
      case None =>
    }
    matched
  }

  override def getRouteMapping(clazz: Class[_], method: String): Option[RouteMapping] = {
    classMappings.get(clazz) match {
      case Some(am) => am.mappings.get(method)
      case None => None
    }
  }

  override def getActionMapping(name: String): Option[ActionMapping] = {
    actionMappings.get(name)
  }
}

/**
 * Find actions from application context
 */
@description("基于Container的Action自动发现者")
class ContainerActionFinder(val container: Container) extends ActionFinder {

  override def actions(actionTest: ActionFinder.Test): Seq[Object] = {
    val actions = new collection.mutable.ListBuffer[Object]
    container.keys foreach { name =>
      val bean: Object = container.getBean(name).get
      if (actionTest.apply(bean.getClass)) actions += bean
    }
    actions.toSeq
  }
}
