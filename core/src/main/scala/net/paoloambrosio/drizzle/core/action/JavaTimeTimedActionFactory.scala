package net.paoloambrosio.drizzle.core.action

import java.time.{Clock, Duration, OffsetDateTime}

import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.{TimedAction, TimedPart}

trait JavaTimeTimedActionFactory extends TimedActionFactory {

  def clock: Clock

  override implicit def timedAction[T](f: TimedPart[T]): TimedAction[T] = {
    scenarioContext: ScenarioContext => {
      val startTime = OffsetDateTime.now(clock)
      val nanoTime = System.nanoTime()
      f(scenarioContext.sessionVariables).map { case (sv, t) =>
        val elapsedTime = Duration.ofNanos(System.nanoTime() - nanoTime)
        (ScenarioContext(Some(ActionTimers(startTime, elapsedTime)), sv), t)
      }
    }
  }
}