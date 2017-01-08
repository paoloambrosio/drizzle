package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.checks.Check

import scala.util.Try

package object checks {

  trait CheckBuilder[R,T] {
    protected def buildCheck(f: T => Any): Check[R] = (sc,r) => Try { f(actual(r)); (sc,r) }
    def actual(response: R): T
    def is(expected: T): Check[R]
  }

}
