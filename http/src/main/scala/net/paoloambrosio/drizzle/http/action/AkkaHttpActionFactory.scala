package net.paoloambrosio.drizzle.http.action

import java.net.URL

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory

import scala.concurrent.ExecutionContext

trait AkkaHttpActionFactory extends HttpActionFactory { this: TimedActionFactory =>

  implicit def system: ActorSystem
  implicit def materializer: ActorMaterializer
  implicit def ec: ExecutionContext

  override def httpGet(url: URL): HttpActionBuilder = new AkkaHttpActionBuilder(HttpMethods.GET, url)
  override def httpPost(url: URL): HttpActionBuilder = new AkkaHttpActionBuilder(HttpMethods.POST, url)


  private class AkkaHttpActionBuilder(method: HttpMethod, url: URL) extends HttpActionBuilder {

    val connectionFlow = {
      val host = url.getHost
      val port = if (url.getPort > 0) url.getPort else url.getDefaultPort
      Http().outgoingConnection(host, port)
    }

    var httpRequest = HttpRequest(method, Uri(url.getPath))

    override def headers(headers: Seq[(String, String)]): HttpActionBuilder = {
      httpRequest = httpRequest.copy(headers = headers.to[collection.immutable.Seq] map { case (n,v) => RawHeader(n,v) })
      this
    }
    override def entity(params: Seq[(String, String)]): HttpActionBuilder = {
      httpRequest = httpRequest.copy(entity = FormData(params:_*).toEntity)
      this
    }

    override def build(): ScenarioAction = timedAction { vars =>
      Source.single(httpRequest).via(connectionFlow).runWith(Sink.head).map(_ => vars)
    }
  }

}
