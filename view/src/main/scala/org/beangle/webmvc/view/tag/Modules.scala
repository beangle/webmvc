package org.beangle.webmvc.view.tag

import org.beangle.commons.inject.bind.{ AbstractBindModule, profile }
import org.beangle.webmvc.view.freemarker.{ FreemarkerManager, FreemarkerViewBuilder, FreemarkerViewResolver, HierarchicalTemplateResolver }
import org.beangle.webmvc.view.tag.freemarker.FreemarkerTemplateEngine

object DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", $("mvc.template_engine.cache", "true"))
    bind("mvc.Taglibrary.c", classOf[CoreTagLibrary])

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[FreemarkerManager])
    bind("mvc.TemplateResolver.freemarker", classOf[HierarchicalTemplateResolver])

    //view
    bind("mvc.ViewResolver.freemarker", classOf[FreemarkerViewResolver])
    bind("mvc.TypeViewBuilder.freemarker", classOf[FreemarkerViewBuilder])
  }
}

@profile("dev")
class DevModule extends AbstractBindModule {
  protected override def binding() {
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", "false")

    bind("mvc.FreemarkerConfigurer.default", classOf[FreemarkerManager]).property("enableCache", "false")
  }
}