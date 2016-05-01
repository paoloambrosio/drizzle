package net.paoloambrosio.drizzle.gatling.http

import java.net.URL

import net.paoloambrosio.drizzle.gatling.core.Action

case class HttpRequest(
  name: String,
  verb: HttpVerb,
  path: String
) extends Action {

}

sealed trait HttpVerb
case object Get extends HttpVerb

class HttpRequestBuilder(name: String) {

  def get(path: String): HttpRequest = HttpRequest(name, Get, path)
}
