package org.beangle.webmvc.context.impl

import java.{ util => jl }
import scala.collection.mutable.{ HashSet, Set }
import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.{ TextBundleRegistry, TextFormater, TextResource }
import org.beangle.webmvc.api.action.EntityActionSupport
import org.beangle.webmvc.api.context.ActionContext
import org.beangle.webmvc.context.ActionContextHelper
import org.beangle.webmvc.execution.impl.MethodHandler
import org.beangle.commons.text.i18n.DefaultTextResource

class ActionTextResource(context: ActionContext, locale: jl.Locale, registry: TextBundleRegistry, formater: TextFormater)
  extends DefaultTextResource(locale, registry, formater) {

  /**
   * 1 remove index key(user.roles[0].name etc.)
   * 2 change ModelDriven to EntitySupport
   * 3 remove superclass and interface lookup
   */
  protected override def get(key: String): Option[String] = {
    if (key == null) ""
    val mapping = ActionContextHelper.getMapping(context)
    val actionClass = mapping.action.config.clazz
    var checked = new HashSet[String]
    // search up class hierarchy
    var msg = getMessage(actionClass.getName, locale, key)
    if (msg != None) return msg
    // nothing still? all right, search the package hierarchy now
    msg = getPackageMessage(actionClass, key, checked)
    if (msg != None) return msg

    if (classOf[EntityActionSupport].isAssignableFrom(actionClass)) {
      // search up model's class hierarchy
      val entityType = mapping.handler.action.asInstanceOf[EntityActionSupport].getEntityType
      if (entityType != null) {
        msg = getPackageMessage(entityType, key, checked)
        if (msg != None) return msg
      }
    }

    // see if it's a child property
    var idx = key.indexOf(".")
    if (idx > 0) {
      var prop = key.substring(0, idx)
      var obj = context.attribute[Any](prop)
      if (null != obj && !prop.equals("action")) {
        var aClass: Class[_] = obj.getClass
        var newKey = key
        var goOn = true
        while (null != aClass && goOn && msg.isEmpty) {
          msg = getPackageMessage(aClass, newKey, checked)
          if (msg.isEmpty) {
            var nextIdx = newKey.indexOf(".", idx + 1)
            if (nextIdx == -1) {
              goOn = false
            } else {
              prop = newKey.substring(idx + 1, nextIdx)
              newKey = newKey.substring(idx + 1)
              idx = nextIdx
              if (Strings.isNotEmpty(prop)) aClass = PropertyUtils.getPropertyType(aClass, prop)
              else aClass = null
            }
          }
        }
      }
    }
    registry.getDefaultText(key, locale)
  }

  private def getPackageMessage(clazz: Class[_], key: String, checked: Set[String]): Option[String] = {
    var msg: Option[String] = None
    var baseName = clazz.getName
    while (baseName.lastIndexOf('.') != -1 && msg.isEmpty) {
      baseName = baseName.substring(0, baseName.lastIndexOf('.'))
      if (!checked.contains(baseName)) {
        msg = getMessage(baseName + ".package", locale, key)
        if (!msg.isEmpty) return msg
        checked += baseName
      }
    }
    None
  }

  /**
   * Gets the message from the named resource bundle.
   */
  private def getMessage(bundleName: String, locale: jl.Locale, key: String): Option[String] = {
    var bundle = registry.load(locale, bundleName)
    if (null == bundle) None else bundle.get(key)
  }
}