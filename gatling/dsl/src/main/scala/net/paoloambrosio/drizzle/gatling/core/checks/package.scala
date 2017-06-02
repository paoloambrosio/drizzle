package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.checks.Check
import net.paoloambrosio.drizzle.core.ScenarioContext
import net.paoloambrosio.drizzle.core.expression.Expression

import scala.util.Try

package object checks {

  trait CheckBuilder[R,T] {
    protected def buildCheck(f: ScenarioContext => T => Try[Unit]): Check[R] =
      (sc,r) => f(sc)(actual(r)).map(_ => (sc,r))

    def actual(response: R): T
    def is(expected: Expression[T]): Check[R]
  }

}
