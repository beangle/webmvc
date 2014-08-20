package org.beangle.webmvc.view.freemarker

import freemarker.template.TemplateHashModelEx
import freemarker.template.SimpleCollection
import freemarker.template.TemplateModel
import freemarker.template.TemplateCollectionModel
import freemarker.template.SimpleScalar
import java.{ util => ju }

class ParametersHashModel(val params: Map[String, Any]) extends TemplateHashModelEx {
  override def get(key: String): TemplateModel = {
    params.get(key) match {
      case Some(v) => {
        if (v.getClass.isArray) {
          val array = v.asInstanceOf[Array[_]]
          if (array.length > 0) {
            new SimpleScalar(array(0).asInstanceOf[String])
          } else {
            null
          }
        } else new SimpleScalar(v.asInstanceOf[String])
      }
      case None => null
    }
  }

  override def isEmpty: Boolean = {
    params.isEmpty
  }

  override def size: Int = {
    params.size
  }

  override def keys: TemplateCollectionModel = {
    import scala.collection.JavaConversions._
    new SimpleCollection(asJavaIterator(params.keys.iterator));
  }

  override def values: TemplateCollectionModel = {
    val iter = params.keys.iterator
    val javaIter = new ju.Iterator[Any]() {
      override def hasNext: Boolean = {
        iter.hasNext
      }
      override def next: Any = {
        params(iter.next)
      }
      override def remove {
        throw new UnsupportedOperationException();
      }
    }

    new SimpleCollection(javaIter)
  }
}
