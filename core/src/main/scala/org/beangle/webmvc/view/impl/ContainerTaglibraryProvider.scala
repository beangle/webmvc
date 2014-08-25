package org.beangle.webmvc.view.impl

import org.beangle.commons.inject.Container
import org.beangle.commons.lang.Strings
import org.beangle.commons.lang.annotation.{ description, spi }
import org.beangle.webmvc.view.tag.{ TagLibrary, TagLibraryProvider }

@description("所有标签库提供者")
class ContainerTaglibraryProvider(container: Container) extends TagLibraryProvider {

  val tagLibraries: Map[String, TagLibrary] = {
    container.getBeans(classOf[TagLibrary]).map {
      case (key, library) =>
        val name = key.toString
        if (name.contains(".")) (Strings.substringAfterLast(name, "."), library) else (name, library)
    }
  }
}