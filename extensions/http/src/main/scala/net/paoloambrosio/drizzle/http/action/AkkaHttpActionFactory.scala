package net.paoloambrosio.drizzle.http.action

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers._
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.{http => drizzle}

import scala.concurrent.{ExecutionContext, Future}

trait AkkaHttpActionFactory extends PostChecksActionFactory[HttpRequest] {
    this: TimedActionFactory =>

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def ec: ExecutionContext


  protected def httpRequestToImplementation(request: drizzle.HttpRequest): HttpRequest = {
    new HttpRequestBuilder()
        .withMethod(request.method)
        .withUri(request.uri)
        .withEntity(request.entity)
        .withHeaders(request.headers)
        .request
  }

  class HttpRequestBuilder(val request: HttpRequest = HttpRequest()) {

    def withMethod(newMethod: drizzle.HttpMethod) = new HttpRequestBuilder(
      request.copy(method = newMethod match {
        case drizzle.HttpMethod.Get => HttpMethods.GET
        case drizzle.HttpMethod.Post => HttpMethods.POST
      })
    )

    def withUri(newUri: String) = new HttpRequestBuilder(
      request.copy(uri = Uri(newUri))
    )

    def withHeaders(headers: Seq[(String, String)]): HttpRequestBuilder = {
      headers.foldLeft(this)({ case (hr, (n, v)) => hr.withHeader(n, v) })
    }

    /**
      * Converts a name/value pair into a header or does special handling
      * if not possible. Not all headers are created equal:
      * {@link akka.http.impl.engine.rendering.HttpRequestRendererFactory}
      *
      * @param n Header name
      * @param v Header value
      */
    private def withHeader(n: String, v: String) = new HttpRequestBuilder(
      n.toLowerCase match {
        case `Content-Type`.lowercaseName => ContentType.parse(v) match {
          case Right(ct) => request.withEntity(request.entity.withContentType(ct))
          case _ => request // TODO Warning unparsable content type
        }
        case `User-Agent`.lowercaseName => request.addHeader(`User-Agent`(v))
        case _ => request.addHeader(RawHeader(n, v))
      }
    )

    def withEntity(newEntity: drizzle.HttpEntity) = new HttpRequestBuilder(
      request.copy(entity = newEntity match {
        case drizzle.HttpEntity.FormParams(params) => FormData(params:_*).toEntity
        case drizzle.HttpEntity.Empty => HttpEntity.Empty
      })
    )
  }

  protected def executeRequest(request: HttpRequest): Future[drizzle.HttpResponse] = {
    // TODO connection pooling
    val connectionFlow = {
      val host = request.uri.authority.host.address()
      val port = request.uri.effectivePort
      Http().outgoingConnection(host, port)
    }
    Source.single(request).via(connectionFlow).runWith(Sink.head).map(r => new AkkaHttpResponse(r))
  }

}

class AkkaHttpResponse(response: HttpResponse) extends drizzle.HttpResponse {

  def status = response.status.intValue()
}
