package net.paoloambrosio.drizzle.gatling.http

import net.paoloambrosio.drizzle.gatling.core.Action

object HttpRequest {

  sealed trait HttpMethod
  case object Get extends HttpMethod
  case object Post extends HttpMethod
}

case class HttpRequest(
  name: String,
  method: HttpRequest.HttpMethod,
  path: String,
  headers: Map[String, String] = Map.empty,
  formParams: Seq[(String, String)] = Seq.empty
) extends Action {

  def headers(extraHeaders: Map[String, String]) = copy(headers = headers ++ extraHeaders)
  def formParam(name: String, value: String) = copy(formParams = formParams :+ (name, value))
}

class HttpRequestFactory(name: String) {

  def get(path: String): HttpRequest = HttpRequest(name, HttpRequest.Get, path)
  def post(path: String): HttpRequest = HttpRequest(name, HttpRequest.Post, path)
}
