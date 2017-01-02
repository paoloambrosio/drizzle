package net.paoloambrosio.drizzle.gatling.core.expression

import net.paoloambrosio.drizzle.core.expression._

import scala.util.Success

object ExpressionSupport {

  def el(s: String): Expression[String] = _ => Success(s) // TODO

}
