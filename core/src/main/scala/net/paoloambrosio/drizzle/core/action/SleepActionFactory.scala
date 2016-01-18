package net.paoloambrosio.drizzle.core.action

import java.time.Duration

import net.paoloambrosio.drizzle.core._

trait SleepActionFactory {

  /**
    * Pause from the end of the previous action.
    *
    * @param duration
    */
  def thinkTime(duration: Duration): ScenarioAction

  /**
    * Pause from the beginning of the previous action, or no pause if the
    * execution time was longer than the pause.
    *
    * @param duration
    */
  def pacing(duration: Duration): ScenarioAction
}
