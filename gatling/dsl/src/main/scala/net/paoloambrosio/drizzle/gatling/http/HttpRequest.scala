package net.paoloambrosio.drizzle.gatling.http

import java.net.URL

import net.paoloambrosio.drizzle.gatling.core.Action

case class HttpRequest(
  name: String,
  verb: HttpVerb,
  path: String,
  headers: Map[String, String] = Map.empty,
  formParams: Seq[FormParam] = Seq.empty
) extends Action {
  def headers(extraHeaders: Map[String, String]) = copy(headers = headers ++ extraHeaders)
  def formParam(name: String, value: String) = copy(formParams = formParams :+ FormParam(name, value))
}

sealed trait HttpVerb
case object Get extends HttpVerb
case object Post extends HttpVerb

case class FormParam(name: String, value: String)

class HttpRequestBuilder(name: String) {

  def get(path: String): HttpRequest = HttpRequest(name, Get, path)
  def post(path: String): HttpRequest = HttpRequest(name, Post, path)
}
