package net.paoloambrosio.drizzle.core

import scala.util.{Success, Try}

package object expression {

  type Expression[T] = ScenarioContext => Try[T]

  object Expression {
    def uninterpreted[T](t: T): Expression[T] = _ => Success(t)
  }

  implicit class ExpressionOps[T](val expression: Expression[T]) extends AnyVal {
    def map[U](f: T => U): Expression[U] = expression.andThen(_.map(f))
  }

}
