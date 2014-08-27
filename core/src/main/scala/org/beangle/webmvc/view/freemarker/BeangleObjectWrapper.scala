package org.beangle.webmvc.view.freemarker

import java.beans.PropertyDescriptor
import java.lang.reflect.{ Method, Modifier }
import java.{ util => ju }

import scala.collection.JavaConversions

import freemarker.core.CollectionAndSequence
import freemarker.ext.beans.BeansWrapper
import freemarker.ext.beans.BeansWrapper.MethodAppearanceDecision
import freemarker.ext.beans.MapModel
import freemarker.ext.util.ModelFactory
import freemarker.template.{ AdapterTemplateModel, DefaultObjectWrapper, ObjectWrapper, SimpleCollection, SimpleDate, SimpleNumber, SimpleScalar, SimpleSequence, TemplateBooleanModel, TemplateCollectionModel, TemplateHashModelEx, TemplateMethodModelEx, TemplateModel }

class BeangleObjectWrapper(val altMapWrapper: Boolean) extends DefaultObjectWrapper {

  protected override def finetuneMethodAppearance(clazz: Class[_], m: Method, decision: MethodAppearanceDecision) {
    val name = m.getName
    if (name.equals("hashCode") || name.equals("toString")) return
    if (isPropertyMethod(m)) {
      val pd = new PropertyDescriptor(name, m, null)
      decision.setExposeAsProperty(pd)
      decision.setExposeMethodAs(name)
      decision.setMethodShadowsProperty(false)
    }
  }

  override def wrap(obj: Any): TemplateModel = {
    if (obj == null) return null
    obj match {
      case tm: TemplateModel => tm
      case s: String => new SimpleScalar(s)
      case num: Number => new SimpleNumber(num)
      case date: ju.Date => {
        date match {
          case sdate: java.sql.Date => new SimpleDate(sdate)
          case stime: java.sql.Time => new SimpleDate(stime)
          case stimestamp: java.sql.Timestamp => new SimpleDate(stimestamp)
          case _ => new SimpleDate(date, getDefaultDateType())
        }
      }
      //scala some/none/collection
      case Some(p) => wrap(p)
      case None => null
      case seq: collection.Seq[_] => new SimpleSequence(JavaConversions.seqAsJavaList(seq), this)
      case set: collection.Set[_] => new SimpleSequence(JavaConversions.setAsJavaSet(set), this)
      case map: collection.Map[_, _] =>
        val juMap = JavaConversions.mapAsJavaMap(map)
        if (altMapWrapper) {
          new FriendlyMapModel(juMap, this)
        } else {
          new MapModel(juMap, this)
        }
      case iter: Iterable[_] => new SimpleSequence(JavaConversions.asJavaCollection(iter), this)

      case b: java.lang.Boolean => if (b) TemplateBooleanModel.TRUE else TemplateBooleanModel.FALSE
      case array: Array[_] => new SimpleSequence(ju.Arrays.asList(array: _*), this)

      case collection: ju.Collection[_] => new SimpleSequence(collection, this)
      case map: ju.Map[_, _] =>
        if (altMapWrapper) {
          new FriendlyMapModel(map, this)
        } else {
          new MapModel(map, this)
        }
      case iter: ju.Iterator[_] => new SimpleCollection(iter, this)
      case _ => handleUnknownType(obj)
    }
  }

  private def isPropertyMethod(m: Method): Boolean = {
    val name = m.getName
    return (m.getParameterTypes().length == 0 && classOf[Unit] != m.getReturnType() && Modifier.isPublic(m.getModifiers())
      && !Modifier.isStatic(m.getModifiers()) && !Modifier.isSynchronized(m.getModifiers()) && !name.startsWith("get") && !name.startsWith("is"))
  }
  // attempt to get the best of both the SimpleMapModel and the MapModel of FM.
  override protected def getModelFactory(clazz: Class[_]): ModelFactory = {
    if (altMapWrapper && classOf[java.util.Map[_, _]].isAssignableFrom(clazz)) FriendlyMapModelFactory
    else super.getModelFactory(clazz)
  }
}

object FriendlyMapModelFactory extends ModelFactory {
  override def create(obj: Object, wrapper: ObjectWrapper): TemplateModel = {
    new FriendlyMapModel(obj.asInstanceOf, wrapper.asInstanceOf)
  }
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
    new CollectionAndSequence(new SimpleSequence((`object`.asInstanceOf[java.util.Map[_, _]]).values(), wrapper))
  }
}

