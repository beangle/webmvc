package org.beangle.webmvc

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.view.tag.BeangleTagLibrary
import org.beangle.webmvc.view.tag.freemarker.FreemarkerTemplateEngine

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", $("mvc.template_engine.cache", "true"))
    bind("mvc.Taglibrary.b", classOf[BeangleTagLibrary])
  }
}