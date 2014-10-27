package org.beangle.webmvc.api.view

import org.beangle.webmvc.api.action.To

trait View {}

class ActionView(val to: To) extends View
class ForwardActionView(to: To) extends ActionView(to)
class RedirectActionView(to: To) extends ActionView(to)