package org.beangle.webmvc.view.tag

import org.beangle.commons.inject.bind.{ AbstractBindModule, profile }
import org.beangle.webmvc.view.freemarker.{ FreemarkerViewBuilder, FreemarkerViewResolver, HierarchicalTemplateResolver, WebFreemarkerConfigurer }
import org.beangle.webmvc.view.tag.freemarker.FreemarkerTemplateEngine

class DefaultModule extends AbstractBindModule {

  protected override def binding() {
    //config
    bind("mvc.TemplateEngine.freemarker", classOf[FreemarkerTemplateEngine]).property("enableCache", $("mvc.template_engine.cache", "true"))
    bind("mvc.Taglibrary.b", classOf[BeangleTagLibrary])

    //template
    bind("mvc.FreemarkerConfigurer.default", classOf[WebFreemarkerConfigurer])
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

    bind("mvc.FreemarkerConfigurer.default", classOf[WebFreemarkerConfigurer]).property("enableCache", "false")
  }
}