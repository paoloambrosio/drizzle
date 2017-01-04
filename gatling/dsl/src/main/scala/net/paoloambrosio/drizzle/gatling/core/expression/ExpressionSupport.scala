package net.paoloambrosio.drizzle.gatling.core.expression

import net.paoloambrosio.drizzle.core.expression._

import scala.reflect.ClassTag


object ExpressionSupport {

  implicit def stringToExpression[T : ClassTag](s: String): Expression[T] = s.el[T]

  implicit class El(val string: String) extends AnyVal {
    def el[T : ClassTag]: Expression[T] = ExpressionCompiler.compile[T](string)
  }

}