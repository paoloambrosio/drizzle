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
    def compose(h1: HttpCheck, h2: HttpCheck): HttpCheck = (sc, r) => for {
      (sc1, r1) <- h1(sc, r)
      (sc2, r2) <- h2(sc1, r1)
    } yield (sc2, r2)

    checks
      .reduceOption((h1, h2) => compose(h1, h2))
      .getOrElse((sc, r) => Success((sc, r)))
  }

  protected def httpRequestToImplementation(request: HttpRequest): T

  protected def executeRequest(builder: T): Future[HttpResponse]
}
