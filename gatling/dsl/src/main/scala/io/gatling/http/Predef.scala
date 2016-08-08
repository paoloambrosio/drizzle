package io.gatling.http

import net.paoloambrosio.drizzle.gatling.http.checks.HttpChecks
import net.paoloambrosio.drizzle.gatling.http.{HttpProtocol, HttpRequestFactory}

/**
  * Gatling HTTP DSL
  */
object Predef extends HttpChecks {

  def http = HttpProtocol()

  def http(name: String) = new HttpRequestFactory(name)

}
