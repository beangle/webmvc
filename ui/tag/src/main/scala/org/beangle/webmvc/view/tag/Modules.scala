package org.beangle.webmvc.view.tag

import org.beangle.commons.inject.bind.AbstractBindModule
import org.beangle.webmvc.view.tag.freemarker.FreemarkerTemplateEngine
import org.beangle.commons.inject.bind.profile

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", $("mvc.template_engine.cache", "true"))
    bind("mvc.Taglibrary.b", classOf[BeangleTagLibrary])
  }
}

@profile("dev")
class DevModule extends AbstractBindModule {
  protected override def binding() {
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", "false")
  }
}