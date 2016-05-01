package net.paoloambrosio.drizzle.gatling.http

import java.net.URL

import net.paoloambrosio.drizzle.gatling.core.Protocol

/**
  * HTTP configuration as it's called by Gatling
  *
  * @param baseURLs request base URLs (load will be distributed across them)
  * @param headers request headers
  */
case class HttpProtocol(
  baseURLs: List[URL] = List.empty,
  headers: Map[String, String] = Map.empty
) extends Protocol {

  def baseURL(url: String): HttpProtocol = baseURLs(List(url))
  def baseURLs(urls: String*): HttpProtocol = baseURLs(urls.toList)
  def baseURLs(urls: List[String]): HttpProtocol = copy(urls.map(new URL(_)))

  def acceptHeader(value: String): HttpProtocol = header("Accept", value)
  def doNotTrackHeader(value: String): HttpProtocol = header("DNT", value)
  def acceptLanguageHeader(value: String): HttpProtocol = header("Accept-Language", value)
  def acceptEncodingHeader(value: String): HttpProtocol = header("Accept-Encoding", value)
  def userAgentHeader(value: String): HttpProtocol = header("User-Agent", value)
  def header(name: String, value: String) = copy(headers = headers + (name -> value))

}
