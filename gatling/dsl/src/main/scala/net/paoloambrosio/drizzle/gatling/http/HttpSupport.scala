package net.paoloambrosio.drizzle.gatling.http

import net.paoloambrosio.drizzle.gatling.core.GatlingAction
import net.paoloambrosio.drizzle.gatling.core.expression.ExpressionSupport._
import net.paoloambrosio.drizzle.http.checks.HttpCheck
import net.paoloambrosio.drizzle.http.{HttpMethod, HttpRequestBuilder}


trait HttpSupport {

  def http = HttpProtocol()

  def http(name: String) = new {
    def get(uri: String) = HttpAction(HttpRequestBuilder(el(name), HttpMethod.Get, el(uri)))
    def post(uri: String) = HttpAction(HttpRequestBuilder(el(name), HttpMethod.Post, el(uri)))
  }
}

case class HttpAction(hrb: HttpRequestBuilder) extends GatlingAction {

  def headers(newHeaders: Map[String, String]) = new HttpAction(newHeaders.foldLeft(hrb) {
    case (a, (n, v)) => a.withHeader(el(n), el(v))
  })

  def formParam(name: String, value: String) = new HttpAction(hrb.withFormParam(el(name), el(value)))

  def check(extraChecks: HttpCheck*) = new HttpAction(hrb.withCheck(extraChecks))
}