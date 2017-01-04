package net.paoloambrosio.drizzle.http.action

import net.paoloambrosio.drizzle.core.ScenarioAction
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.{PostTimedPart, PreTimedPart, TimedPart}
import net.paoloambrosio.drizzle.core.expression._
import net.paoloambrosio.drizzle.http.checks.HttpCheck
import net.paoloambrosio.drizzle.http.{HttpRequest, HttpResponse}

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

trait PostChecksActionFactory[T] extends HttpActionFactory { this: TimedActionFactory =>

  def httpRequest(requestEx: Expression[HttpRequest], checks: Seq[HttpCheck]): ScenarioAction = {
    timedAction(pre(requestEx), timedPart, post(checks))
  }

  private def pre(requestEx: Expression[HttpRequest]): PreTimedPart[Try[T]] = {
    requestEx.map(httpRequestToImplementation)
  }

  private def timedPart: TimedPart[Try[T], HttpResponse] = {
    case Success(builder) => executeRequest(builder)
    case Failure(e) => Future.failed(e)
  }

  private def post(checks: Seq[HttpCheck]): PostTimedPart[HttpResponse] = {
    Function.untupled(
      checks.map(_.tupled).reduceOption(_ andThen _).getOrElse(identity)
    )
  }

  protected def httpRequestToImplementation(request: HttpRequest): T

  protected def executeRequest(builder: T): Future[HttpResponse]
}
