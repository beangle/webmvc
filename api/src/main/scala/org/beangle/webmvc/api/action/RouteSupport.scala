/*
 * Beangle, Agile Development Scaffold and Toolkit
 *
 * Copyright (c) 2005-2015, Beangle Software.
 *
 * Beangle is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Beangle is distributed in the hope that it will be useful.
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Beangle.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.beangle.webmvc.api.action

import org.beangle.webmvc.api.view.ForwardActionView
import org.beangle.webmvc.api.view.RedirectActionView
import org.beangle.commons.lang.Strings
import org.beangle.webmvc.api.view.View
import org.beangle.webmvc.api.annotation.ignore

trait RouteSupport extends MessageSupport {

  @ignore
  protected final def forward(view: String = null): String = {
    view
  }

  @ignore
  protected final def forward(view: String, message: String): String = {
    addMessage(getText(message))
    view
  }

  @ignore
  protected final def forward(action: To): View = {
    new ForwardActionView(action)
  }

  @ignore
  protected final def forward(action: To, message: String): View = {
    if (Strings.isNotBlank(message)) {
      if (Strings.contains(message, "error")) addError(message)
      else addMessage(message)
    }
    new ForwardActionView(action)
  }

  @ignore
  protected final def to(obj: Object, method: String): ToClass = {
    new ToClass(obj.getClass, method)
  }

  @ignore
  protected final def to(obj: Object, method: String, params: String): ToClass = {
    new ToClass(obj.getClass, method).params(params)
  }

  @ignore
  protected final def to(clazz: Class[_], method: String): ToClass = {
    new ToClass(clazz, method)
  }

  @ignore
  protected final def to(clazz: Class[_], method: String, params: String): ToClass = {
    new ToClass(clazz, method).params(params)
  }

  @ignore
  protected final def to(uri: String, params: String): ToURL = {
    new ToURL(uri).params(params)
  }

  @ignore
  protected final def to(uri: String): ToURL = {
    To(uri)
  }

  @ignore
  protected final def redirect(method: String): View = {
    redirect(to(this, method), null)
  }

  @ignore
  protected final def redirect(method: String, message: String): View = {
    redirect(to(this, method), message)
  }

  @ignore
  protected final def redirect(method: String, params: String, message: String): View = {
    redirect(to(this, method, params), message)
  }

  @ignore
  protected final def redirect(action: To, message: String): View = {
    if (Strings.isNotEmpty(message)) addFlashMessage(message)
    new RedirectActionView(action)
  }

}