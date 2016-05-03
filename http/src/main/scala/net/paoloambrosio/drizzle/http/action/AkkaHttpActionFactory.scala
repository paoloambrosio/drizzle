package net.paoloambrosio.drizzle.http.action
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.model.headers.RawHeader
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.{Sink, Source}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.http.model.{FormUrlEncodedEntity, Get, NoEntity, Post, HttpRequest => DrizzleHttpRequest}

import scala.concurrent.{ExecutionContext, Future}

trait AkkaHttpActionFactory extends HttpActionFactory { this: TimedActionFactory =>

  implicit def system: ActorSystem
  implicit val materializer = ActorMaterializer()
  implicit def ec: ExecutionContext

  override def httpAction(request: DrizzleHttpRequest): ScenarioAction = timedAction(httpCall(request))

  private def httpCall(request: DrizzleHttpRequest): SessionVariables => Future[SessionVariables] = { vars =>
    val host = request.url.getHost
    val port = if (request.url.getPort > 0) request.url.getPort else request.url.getDefaultPort
    val connectionFlow = Http().outgoingConnection(host, port)
    Source.single(toAkkaRequest(request)).via(connectionFlow).runWith(Sink.head).map(_ => vars)
  }

  def toAkkaRequest(request: DrizzleHttpRequest): HttpRequest = {
    val method = request.verb match {
      case Get => HttpMethods.GET
      case Post => HttpMethods.POST
    }
    val uri = Uri(request.url.getPath)
    val headers = request.headers.to[collection.immutable.Seq] map { case (n,v) => RawHeader(n,v) }
    val entity = request.entity match {
      case NoEntity => HttpEntity.Empty
      case FormUrlEncodedEntity(params) => FormData(params:_*).toEntity
    }
    HttpRequest(method, uri, headers, entity)
  }
}
