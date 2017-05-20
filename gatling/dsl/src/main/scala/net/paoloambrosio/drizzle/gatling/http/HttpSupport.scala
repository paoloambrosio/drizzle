package net.paoloambrosio.drizzle.gatling.http

import net.paoloambrosio.drizzle.gatling.core.GatlingAction
import net.paoloambrosio.drizzle.gatling.core.expression.ExpressionSupport
import net.paoloambrosio.drizzle.http.checks.HttpCheck
import net.paoloambrosio.drizzle.http.{HttpMethod, HttpRequestBuilder}


trait HttpSupport extends ExpressionSupport {

  def http = HttpProtocol()

  def http(name: String) = new {
    def get(uri: String) = HttpAction(HttpRequestBuilder(name, HttpMethod.Get, uri))
    def post(uri: String) = HttpAction(HttpRequestBuilder(name, HttpMethod.Post, uri))
  }
}

case class HttpAction(hrb: HttpRequestBuilder) extends GatlingAction with ExpressionSupport {

  def headers(newHeaders: Map[String, String]) = new HttpAction(newHeaders.foldLeft(hrb) {
    case (a, (n, v)) => a.withHeader(n, v)
  })

  def formParam(name: String, value: String) = new HttpAction(hrb.withFormParam(name, value))

  def check(extraChecks: HttpCheck*) = new HttpAction(hrb.withCheck(extraChecks))
}