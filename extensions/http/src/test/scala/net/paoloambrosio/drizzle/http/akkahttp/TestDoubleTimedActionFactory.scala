package net.paoloambrosio.drizzle.http.akkahttp

import java.time.{Duration, OffsetDateTime}

import net.paoloambrosio.drizzle.core.ActionTimers
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.TimedPart

trait TestDoubleTimedActionFactory extends TimedActionFactory {

  val constantTimers: ActionTimers = ActionTimers(OffsetDateTime.MIN, Duration.ZERO)

  override def timed[I, O](f: TimedPart[I, O]): TimedPart[I, (ActionTimers, O)] = {
    f andThen (_.map((constantTimers, _)))
  }
}
