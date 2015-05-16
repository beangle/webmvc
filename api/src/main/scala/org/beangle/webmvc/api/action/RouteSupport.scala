package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.view.ForwardActionView
import org.beangle.webmvc.api.view.RedirectActionView
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.api.view.View

trait RouteSupport extends MessageSupport {

  protected final def forward(view: String = null): String = {
    view
  }

  protected final def forward(view: String, message: String): String = {
    addMessage(getText(message))
    view
  }

  protected final def forward(action: To): View = {
    new ForwardActionView(action)
  }

  protected final def forward(action: To, message: String): View = {
    if (Strings.isNotBlank(message)) {
      if (Strings.contains(message, "error")) addError(message)
      else addMessage(message)
    }
    new ForwardActionView(action)
  }

  protected final def to(obj: Object, method: String): ToClass = {
    new ToClass(obj.getClass, method)
  }

  protected final def to(obj: Object, method: String, params: String): ToClass = {
    new ToClass(obj.getClass, method).params(params)
  }

  protected final def to(clazz: Class[_], method: String): ToClass = {
    new ToClass(clazz, method)
  }

  protected final def to(clazz: Class[_], method: String, params: String): ToClass = {
    new ToClass(clazz, method).params(params)
  }

  protected final def to(uri: String, params: String): ToURL = {
    new ToURL(uri).params(params)
  }

  protected final def to(uri: String): ToURL = {
    To(uri)
  }

  protected final def redirect(method: String): View = {
    redirect(to(this, method), null)
  }

  protected final def redirect(method: String, message: String): View = {
    redirect(to(this, method), message)
  }

  protected final def redirect(method: String, params: String, message: String): View = {
    redirect(to(this, method, params), message)
  }

  protected final def redirect(action: To, message: String): View = {
    if (Strings.isNotEmpty(message)) addFlashMessage(message)
    new RedirectActionView(action)
  }

}