package net.paoloambrosio.drizzle.core

import java.time.{Clock, Duration, OffsetDateTime}

import scala.concurrent.{ExecutionContext, Future}

trait TimedActionFactory {

  def timedAction(f: SessionVariables => Future[SessionVariables])(implicit ec: ExecutionContext, clock: Clock): ScenarioAction = {
    scenarioContext: ScenarioContext => {
      val startTime = OffsetDateTime.now(clock)
      val nanoTime = System.nanoTime()
      f(scenarioContext.sessionVariables).map { sv =>
        val elapsedTime = Duration.ofNanos(System.nanoTime() - nanoTime)
        ScenarioContext(ActionTimers(startTime, elapsedTime), sv)
      }
    }
  }
}