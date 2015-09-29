package org.beangle.webmvc.api.i18n

/**
 * @author chaostone
 */
trait TextProvider {
  /**
   * Gets a message based on a message key, or null if no message is found.
   */
  def apply(key: String): Option[String]

  /**
   * Gets a message based on a key using the supplied obj, as defined in
   * {@link java.text.MessageFormat}, or, if the message is not found, a
   * supplied default value is returned.
   */
  def apply(key: String, defaultValue: String, obj: Any*): String
}