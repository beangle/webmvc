package org.beangle.webmvcx.struts2.util

import org.beangle.commons.script.{ EvaluationException, ExpressionEvaluator }

import ognl.{ Ognl, OgnlException }

class OgnlExpressionEvaluator[T] extends ExpressionEvaluator {

  private val trees = new collection.mutable.WeakHashMap[String, Object]

  @throws(classOf[EvaluationException])
  def parse(exp: String) {
    try {
      trees.put(exp, Ognl.parseExpression(exp))
    } catch {
      case e: OgnlException => throw new EvaluationException(e.getMessage(), e.getCause())
    }
  }

  /**
   * Eval a expression within context
   */
  def eval(exp: String, root: Object): Object = {
    try {
      trees.get(exp) match {
        case Some(tree) => Ognl.getValue(tree, root)
        case None =>
          val tree = Ognl.parseExpression(exp)
          trees.put(exp, tree)
          Ognl.getValue(tree, root)
      }
    } catch {
      case e: OgnlException => throw new EvaluationException(e.getMessage(), e.getCause())
    }
  }

  def eval[T](exp: String, root: Object, resultType: Class[T]): T = {
    try {
      trees.get(exp) match {
        case Some(tree) => Ognl.getValue(tree, root, resultType).asInstanceOf[T]
        case None =>
          val tree = Ognl.parseExpression(exp)
          trees.put(exp, tree)
          Ognl.getValue(tree, root, resultType).asInstanceOf[T]
      }
    } catch {
      case e: OgnlException => throw new EvaluationException(e.getMessage(), e.getCause())
    }
  }

}