package org.beangle.webmvc.view.component

class Email(context: ComponentContext) extends AbstractTextBean(context) {
  check = "match('email')"
}

class Password(context: ComponentContext) extends AbstractTextBean(context) {
  var minlength: String = "6"
  maxlength = "10"
  var showStrength = "false"
}