package net.paoloambrosio.drizzle.http


case class HttpRequest (
  method: HttpMethod,
  uri: String,
  headers: Seq[(String, String)] = Seq.empty,
  entity: HttpEntity = HttpEntity.Empty
) {
  import HttpEntity._

  def withHeader(name: String, value: String) = copy(headers =
    headers :+ (name, value)
  )

  def withFormParam(name: String, value: String) = copy(entity =
    FormParams(entity match {
      case Empty => Seq((name, value))
      case FormParams(params) => params :+ (name, value)
    })
  )

}

sealed trait HttpMethod
object HttpMethod {
  case object Get extends HttpMethod
  case object Post extends HttpMethod
}

sealed trait HttpEntity
object HttpEntity {
  case object Empty extends HttpEntity
  case class FormParams(params: Seq[(String, String)]) extends HttpEntity
}