package org.beangle.webmvc.dispatch

import org.beangle.webmvc.config.ActionMapping
import org.beangle.webmvc.execution.Handler

class RequestMapping(val action: ActionMapping, val handler: Handler, val params: collection.Map[String, Any])

