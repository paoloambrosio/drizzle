package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.checks.Check

package object checks {

  trait CheckBuilder[R,T] {
    protected def buildCheck(f: R => Any): Check[R] = (sc,r) => { f(r); (sc,r) }
    def actual(response: R): T
    def is(expected: T): Check[R]
  }

}
