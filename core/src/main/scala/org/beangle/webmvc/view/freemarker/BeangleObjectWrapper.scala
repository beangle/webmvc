package org.beangle.webmvc.view.freemarker

import freemarker.template.TemplateModelException
import freemarker.template.TemplateModel
import freemarker.template.DefaultObjectWrapper
import freemarker.ext.beans.CollectionModel
import java.util.Collection
import freemarker.template.SimpleSequence
import freemarker.ext.beans.MapModel
import freemarker.template.TemplateHashModelEx
import freemarker.template.TemplateMethodModelEx
import freemarker.template.AdapterTemplateModel
import freemarker.ext.beans.BeansWrapper
import freemarker.ext.util.ModelFactory
import freemarker.template.ObjectWrapper
import freemarker.template.TemplateCollectionModel
import freemarker.core.CollectionAndSequence
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecision
import java.beans.PropertyDescriptor
import scala.collection.JavaConversions
import java.lang.reflect.Method
import java.lang.reflect.Modifier

class BeangleObjectWrapper(val altMapWrapper: Boolean) extends DefaultObjectWrapper {

  /**
   * 特殊包装set和map
   */
  @throws(classOf[TemplateModelException])
  private def inner_wrap(obj: Object): TemplateModel = {
    if (obj == null) { return super.wrap(null); }
    if (obj.isInstanceOf[List[_]]) { return new CollectionModel(obj.asInstanceOf[Collection[_]], this); }
    // 使得set等集合可以排序
    if (obj.isInstanceOf[Collection[_]]) {
      new SimpleSequence(obj.asInstanceOf[Collection[_]], this);
    } else {
      if (obj.isInstanceOf[Map[_, _]]) {
        if (altMapWrapper) {
          new FriendlyMapModel(obj.asInstanceOf, this);
        } else {
          new MapModel(obj.asInstanceOf, this);
        }
      } else super.wrap(obj);
    }
  }

  protected override def finetuneMethodAppearance(clazz: Class[_], m: Method,
    decision: MethodAppearanceDecision) {
    val name = m.getName
    if (name.equals("hashCode") || name.equals("toString")) return
    if (isPropertyMethod(m)) {
      val pd = new PropertyDescriptor(name, m, null);
      decision.setExposeAsProperty(pd)
      decision.setExposeMethodAs(name)
      decision.setMethodShadowsProperty(false)
    }
  }
  override def wrap(obj: Object): TemplateModel = {
    return inner_wrap(convert2Java(obj));
  }

  private def convert2Java(obj: Any): Object = {
    obj match {
      case Some(inner) => convert2Java(inner)
      case None => null
      case seq: collection.Seq[_] => JavaConversions.seqAsJavaList(seq)
      case map: collection.Map[_, _] => JavaConversions.mapAsJavaMap(map)
      case iter: Iterable[_] => JavaConversions.asJavaIterable(iter)
      case _ => obj.asInstanceOf[Object]
    }
  }

  private def isPropertyMethod(m: Method): Boolean = {
    val name = m.getName
    return (m.getParameterTypes().length == 0 && classOf[Unit] != m.getReturnType() && Modifier.isPublic(m.getModifiers())
      && !Modifier.isStatic(m.getModifiers()) && !Modifier.isSynchronized(m.getModifiers()) && !name.startsWith("get") && !name.startsWith("is"))
  }
  // attempt to get the best of both the SimpleMapModel and the MapModel
  // of FM.
  override protected def getModelFactory(clazz: Class[_]): ModelFactory = {
    if (altMapWrapper && classOf[java.util.Map[_, _]].isAssignableFrom(clazz)) {
      FriendlyMapModel.FACTORY
    } else super.getModelFactory(clazz);
  }
}

object FriendlyMapModel {
  val FACTORY: ModelFactory = new ModelFactory() {
    def create(obj: Object, wrapper: ObjectWrapper): TemplateModel = {
      new FriendlyMapModel(obj.asInstanceOf, wrapper.asInstanceOf);
    }
  };
}
/**
 * Attempting to get the best of both worlds of FM's MapModel and
 * simplemapmodel, by reimplementing the isEmpty(), keySet() and values()
 * methods. ?keys and ?values built-ins are thus available, just as well as
 * plain Map methods.
 */
class FriendlyMapModel(map: java.util.Map[_, _], wrapper: BeansWrapper)
  extends MapModel(map, wrapper) with TemplateHashModelEx
  with TemplateMethodModelEx with AdapterTemplateModel {

  // Struts2将父类的&& super.isEmpty()省去了，原因不知
  override def isEmpty(): Boolean = {
    `object`.asInstanceOf[java.util.Map[_, _]].isEmpty()
  }

  // 此处实现与MapModel不同，MapModel中复制了一个集合
  // 影响了?keySet,?size方法
  override protected def keySet(): java.util.Set[_] = {
    `object`.asInstanceOf[java.util.Map[_, _]].keySet()
  }

  // add feature
  override def values(): TemplateCollectionModel = {
    new CollectionAndSequence(new SimpleSequence((`object`.asInstanceOf[java.util.Map[_, _]]).values(), wrapper));
  }
}

