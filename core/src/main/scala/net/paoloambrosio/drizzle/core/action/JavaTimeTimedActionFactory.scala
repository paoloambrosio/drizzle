package net.paoloambrosio.drizzle.core.action

import java.time.{Clock, Duration, OffsetDateTime}

import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.TimedPart

trait JavaTimeTimedActionFactory extends TimedActionFactory {

  def clock: Clock

  override def timed[I,O](f: TimedPart[I,O]): TimedPart[I,(ActionTimers, O)] = input => {
    val startTime = OffsetDateTime.now(clock)
    val nanoTime = System.nanoTime()
    f(input).map { output =>
      val elapsedTime = Duration.ofNanos(System.nanoTime() - nanoTime)
      (ActionTimers(startTime, elapsedTime), output)
    }
  }
}