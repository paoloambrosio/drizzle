package net.paoloambrosio.drizzle.gatling.core.expression

import net.paoloambrosio.drizzle.core.expression._

import scala.reflect.ClassTag
import scala.util.{Success, Try}

trait ExpressionSupport {

  import ExpressionSupport._

  implicit def stringToExpression[T : ClassTag](s: String): Expression[T] = s.el[T]
  implicit def typeToExpression[T : ClassTag](t: T): Expression[T] = s => Success(t)
  implicit def tryWrapper[T : ClassTag](t: T): Try[T] = Success(t)
}

object ExpressionSupport {

  implicit class El(val string: String) extends AnyVal {
    def el[T : ClassTag]: Expression[T] = ExpressionCompiler.compile[T](string)
  }

}