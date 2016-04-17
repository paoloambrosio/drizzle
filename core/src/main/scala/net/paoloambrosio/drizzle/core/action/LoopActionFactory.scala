package net.paoloambrosio.drizzle.core.action

import java.time.Duration

import net.paoloambrosio.drizzle.core._

trait LoopActionFactory {

  /**
    * Infinite loop over actions.
    *
    * @param sub Actions to be repeated
    */
  def forever(sub: Stream[ScenarioAction]): Stream[ScenarioAction] = {
    Stream.continually(sub).flatten
  }

  /**
    * Finite loop over actions.
    *
    * @param sub Actions to be repeated
    */
  def repeat(count: Int)(sub: Stream[ScenarioAction]): Stream[ScenarioAction] = {
    Stream.fill(count)(sub).flatten
  }
}
