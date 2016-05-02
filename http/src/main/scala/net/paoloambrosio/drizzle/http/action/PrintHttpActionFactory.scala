package net.paoloambrosio.drizzle.http.action
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.http.model._

import scala.concurrent.Future

trait PrintHttpActionFactory extends HttpActionFactory { this: TimedActionFactory =>
  override def httpAction(request: HttpRequest): ScenarioAction = timedAction(httpCall(request))

  private def httpCall(request: HttpRequest): SessionVariables => Future[SessionVariables] = { vars =>
    println(s"HTTP ${request.verb} ${request.url}")
    for ((k,v) <- request.headers) println(s"  $k: $v")
    request.entity match {
      case NoEntity =>
      case FormUrlEncodedEntity(params) => println(s"  $params")
    }
    Future.successful(vars)
  }
}
