package net.paoloambrosio.drizzle.core

import scala.util.{Success, Try}

package object expression {

  type Expression[T] = ScenarioContext => Try[T]

  object Expression {
    def uninterpreted[T](t: T): Expression[T] = _ => Success(t)
  }
}
