package net.paoloambrosio.drizzle.http.akkahttp

import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentType, FormData, HttpHeader, HttpMethod, HttpMethods, HttpRequest, Uri}
import akka.http.scaladsl.model.headers.{RawHeader, `Content-Type`, `User-Agent`}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.http.{HttpActionBuilder, HttpActionFactory, HttpResponse}

import scala.concurrent.{ExecutionContext, Future}

trait AkkaHttpActionFactory extends HttpActionFactory {

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def ec: ExecutionContext

  override def httpGet(url: URL): HttpActionBuilder = new AkkaHttpActionBuilder(HttpMethods.GET, url)
  override def httpPost(url: URL): HttpActionBuilder = new AkkaHttpActionBuilder(HttpMethods.POST, url)


  private class AkkaHttpActionBuilder(method: HttpMethod, url: URL) extends HttpActionBuilder {

    private val connectionFlow = {
      val host = url.getHost
      val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
      Http().outgoingConnection(host, port)
    }

    private var httpRequest = HttpRequest(method, Uri(url.getPath))

    override def headers(headers: Seq[(String, String)]): HttpActionBuilder = {
      headers.foreach { case (n, v) => addHeader(n, v) }
      this
    }

    /**
      * Converts a name/value pair into a header or does special handling
      * if not possible. Not all headers are created equal:
      * {@link akka.http.impl.engine.rendering.HttpRequestRendererFactory}
      *
      * @param n Header name
      * @param v Header value
      */
    private def addHeader(n: String, v: String): Unit = {
      n.toLowerCase match {
        case `Content-Type`.lowercaseName => ContentType.parse(v) match {
          case Right(ct) => httpRequest = httpRequest.withEntity(httpRequest.entity.withContentType(ct))
          case _ => // TODO Warning unparsable content type
        }
        case `User-Agent`.lowercaseName => addHeader(`User-Agent`(v))
        case _ => addHeader(RawHeader(n, v))
      }
    }

    private def addHeader(h: HttpHeader): Unit = {
      httpRequest = httpRequest.addHeader(h)
    }

    override def entity(params: Seq[(String, String)]): HttpActionBuilder = {
      httpRequest = httpRequest.copy(entity = FormData(params:_*).toEntity)
      this
    }

    override def apply(vars: SessionVariables): Future[(SessionVariables, HttpResponse)] = {
      Source.single(httpRequest).via(connectionFlow).runWith(Sink.head).map(r => (vars, new AkkaHttpResponse(r)))
    }
  }

}
