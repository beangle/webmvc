package org.beangle.webmvc.struts2

import java.{ util => jl }

import scala.collection.mutable.{ HashSet, Set }

import org.beangle.commons.bean.PropertyUtils
import org.beangle.commons.lang.Strings
import org.beangle.commons.text.i18n.impl.DefaultTextResource
import org.beangle.commons.text.i18n.spi.{ TextBundleRegistry, TextFormater }
import org.beangle.webmvc.action.EntityActionSupport

import com.opensymphony.xwork2.ActionContext
import com.opensymphony.xwork2.util.ValueStack

class ActionTextResource(val actionClass: Class[_], locale: jl.Locale, registry: TextBundleRegistry,
  formater: TextFormater, val valueStack: ValueStack) extends DefaultTextResource(locale, registry, formater) {

  /**
   * 1 remove index key(user.roles[0].name etc.)
   * 2 change ModelDriven to EntitySupport
   * 3 remove superclass and interface lookup
   */
  protected override def get(key: String, locale: jl.Locale): Option[String] = {
    if (key == null) ""
    var checked = new HashSet[String]
    // search up class hierarchy
    var msg = getMessage(actionClass.getName(), locale, key)
    if (msg != None) return msg
    // nothing still? all right, search the package hierarchy now
    msg = getPackageMessage(actionClass.getName(), key, checked)
    if (msg != None) return msg

    if (classOf[EntityActionSupport].isAssignableFrom(actionClass)) {
      var context = ActionContext.getContext()
      // search up model's class hierarchy
      var actionInvocation = context.getActionInvocation()
      // ActionInvocation may be null if we're being run from a Sitemesh filter
      if (actionInvocation != null) {
        var action = actionInvocation.getAction()
        if (action.isInstanceOf[EntityActionSupport]) {
          var entityName = (action.asInstanceOf[EntityActionSupport]).entityName
          if (entityName != null) {
            msg = getPackageMessage(entityName, key, checked)
            if (msg != None) return msg
          }
        }
      }
    }

    // see if it's a child property
    var idx = key.indexOf(".")
    if (idx > 0) {
      var prop = key.substring(0, idx)
      var obj = valueStack.findValue(prop)
      if (null != obj && !prop.equals("action")) {
        var aClass: Class[_] = obj.getClass()
        var newKey = key
        var goOn = true
        while (null != aClass && goOn && msg.isEmpty) {
          msg = getPackageMessage(aClass.getName(), newKey, checked)
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
    val defaultText = registry.getDefaultText(key, locale)
    if (null == defaultText) None else Some(defaultText)
  }

  private def getPackageMessage(className: String, key: String, checked: Set[String]): Option[String] = {
    var msg: Option[String] = None
    var baseName = className
    while (baseName.lastIndexOf('.') != -1 && msg.isEmpty) {
      baseName = baseName.substring(0, baseName.lastIndexOf('.'))
      if (!checked.contains(baseName)) {
        msg = getMessage(baseName + ".package", locale, key)
        if (!msg.isEmpty) msg
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