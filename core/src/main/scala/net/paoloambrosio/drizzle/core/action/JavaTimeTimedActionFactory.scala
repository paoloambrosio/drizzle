package net.paoloambrosio.drizzle.core.action

import java.time.{Clock, Duration, OffsetDateTime}

import net.paoloambrosio.drizzle.core._

import scala.concurrent.{ExecutionContext, Future}

trait JavaTimeTimedActionFactory extends TimedActionFactory {

  implicit def ec: ExecutionContext
  def clock: Clock

  override def timedAction(f: SessionVariables => Future[SessionVariables]): ScenarioAction = {
    scenarioContext: ScenarioContext => {
      val startTime = OffsetDateTime.now(clock)
      val nanoTime = System.nanoTime()
      f(scenarioContext.sessionVariables).map { sv =>
        val elapsedTime = Duration.ofNanos(System.nanoTime() - nanoTime)
        ScenarioContext(Some(ActionTimers(startTime, elapsedTime)), sv)
      }
    }
  }
}