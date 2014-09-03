package org.beangle.webmvc.view.impl

import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.view.{ TagLibrary, TagLibraryProvider }
import org.beangle.commons.inject.ContainerRefreshedHook

@description("所有标签库提供者")
class ContainerTaglibraryProvider extends TagLibraryProvider with ContainerRefreshedHook {

  var tagLibraries: Map[String, TagLibrary] = Map.empty

  override def notify(container: Container): Unit = {
    tagLibraries = container.getBeans(classOf[TagLibrary]).map {
      case (key, library) =>
        val name = key.toString
        if (name.contains(".")) (Strings.substringAfterLast(name, "."), library) else (name, library)
    }
  }
}