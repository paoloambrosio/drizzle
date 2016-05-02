package net.paoloambrosio.drizzle.http

import java.net.URL

package object model {

  case class HttpRequest(verb: HttpVerb, url: URL, headers: Seq[(String, String)], entity: HttpEntity = NoEntity)

  sealed trait HttpVerb
  case object Get extends HttpVerb
  case object Post extends HttpVerb

  sealed trait HttpEntity
  case object NoEntity extends HttpEntity
  case class FormUrlEncodedEntity(params: Seq[(String, String)]) extends HttpEntity
}
