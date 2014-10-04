package org.beangle.webmvc.config.impl

import org.beangle.commons.inject.Container
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.commons.lang.time.Stopwatch
import org.beangle.commons.logging.Logging
import org.beangle.commons.web.intercept.Interceptor
import org.beangle.webmvc.config.{ ActionConfig, ActionMapping, ActionMappingBuilder, Configurer, Profile, ProfileProvider }
import org.beangle.webmvc.context.ActionFinder

@description("缺省配置器")
class DefaultConfigurer(profileProvider: ProfileProvider, container: Container) extends Configurer with Logging {

  private val class2Profiles = new collection.mutable.HashMap[String, Profile]

  var actionConfigs: Map[String, ActionConfig] = Map.empty

  var profiles: List[Profile] = Nil

  var actionMappingBuilder: ActionMappingBuilder = _

  var actionFinder: ActionFinder = _

  override def build(): Seq[Tuple3[String, ActionMapping, Object]] = {
    val watch = new Stopwatch(true)
    profiles = profileProvider.loadProfiles() map { pc =>
      val interceptors = pc.interceptorNames map { interName =>
        container.getBean[Interceptor](interName).get
      }
      pc.mkProfile(interceptors)
    }
    profiles = profiles.sorted

    var actionCount, mappingCount = 0
    val results = new collection.mutable.ListBuffer[Tuple3[String, ActionMapping, Object]]
    val configs = new collection.mutable.HashMap[String, ActionConfig]
    actionFinder.getActions(new ActionFinder.Test(this)) foreach { bean =>
      val clazz = bean.getClass
      val mappings = actionMappingBuilder.build(clazz, this.getProfile(clazz.getName))
      if (!mappings.isEmpty) {
        mappings.foreach {
          case (url, action) =>
            mappingCount += 1
            results += Tuple3(url, action, bean)
        }
        val action = mappings.head._2
        configs.put(action.config.clazz.getName, action.config)
        configs.put(action.config.name, action.config)
        actionCount += 1
      }
    }
    actionConfigs = configs.toMap
    info(s"Action scan completed,create $actionCount actions($mappingCount mappings) in ${watch}.")
    results
  }

  override def getProfile(className: String): Profile = {
    var matched = class2Profiles.get(className).orNull
    if (null != matched) return matched
    profiles.find(p => !p.matches(className).isEmpty) match {
      case Some(p) =>
        class2Profiles.put(className, p)
        matched = p
        debug(s"${className} match profile:${p}")
      case None =>
    }
    matched
  }

  override def getActionMapping(name: String, method: String): Option[ActionMapping] = {
    actionConfigs.get(name) match {
      case Some(config) => config.mappings.get(method)
      case None => None
    }
  }

  override def getConfig(name: String): Option[ActionConfig] = {
    actionConfigs.get(name)
  }
}
