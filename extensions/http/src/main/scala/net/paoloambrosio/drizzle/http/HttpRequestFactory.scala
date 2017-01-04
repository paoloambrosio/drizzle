package net.paoloambrosio.drizzle.http

import java.net.{URI, URL}

import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.http.checks.HttpCheck

object HttpRequestBuilder {

  def apply(stepNameEx: Expression[String], method: HttpMethod, uriEx: Expression[String]): HttpRequestBuilder =
    new HttpRequestBuilder(stepNameEx, uriEx.andThen(_.map(HttpRequest(method, _))))

}

case class HttpRequestBuilder(
  val stepNameEx: Expression[String],
  val requestEx: Expression[HttpRequest],
  val checks: Seq[HttpCheck] = Seq.empty
) {

  def withHeader(nameEx: Expression[String], valueEx: Expression[String]) = copy(requestEx = { sc =>
    for {
      name <- nameEx(sc)
      value <- valueEx(sc)
      request <- requestEx(sc)
    } yield request.withHeader(name, value)
  })

  def withFormParam(nameEx: Expression[String], valueEx: Expression[String]) = copy(requestEx = { sc =>
    for {
      name <- nameEx(sc)
      value <- valueEx(sc)
      request <- requestEx(sc)
    } yield request.withFormParam(name, value)
  })

  def withCheck(extraChecks: Seq[HttpCheck]) = copy(checks = checks ++ extraChecks)
}
