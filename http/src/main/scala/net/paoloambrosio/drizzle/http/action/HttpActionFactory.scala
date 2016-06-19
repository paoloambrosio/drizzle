package net.paoloambrosio.drizzle.http.action

import java.net.URL

import net.paoloambrosio.drizzle.core.ScenarioAction

trait HttpActionFactory {

  def httpGet(url: URL): HttpActionBuilder
  def httpPost(url: URL): HttpActionBuilder
}

trait HttpActionBuilder {

  def headers(headers: Seq[(String, String)]): HttpActionBuilder
  def entity(params: Seq[(String, String)]): HttpActionBuilder

  def build(): ScenarioAction
}
