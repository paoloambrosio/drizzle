package io.gatling.http

import net.paoloambrosio.drizzle.gatling.http.{HttpProtocol, HttpRequestBuilder}

/**
  * Gatling HTTP DSL
  */
object Predef {

  def http = HttpProtocol()

  def http(name: String) = new HttpRequestBuilder(name)

}
