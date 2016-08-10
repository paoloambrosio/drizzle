package net.paoloambrosio.drizzle.http

import java.net.URL

import net.paoloambrosio.drizzle.core.action.TimedActionFactory._

trait HttpActionFactory {

  def httpGet(url: URL): HttpActionBuilder
  def httpPost(url: URL): HttpActionBuilder
}

trait HttpActionBuilder extends TimedPart[HttpResponse] {

  def headers(headers: Seq[(String, String)]): HttpActionBuilder
  def entity(params: Seq[(String, String)]): HttpActionBuilder
}
