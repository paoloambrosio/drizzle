package net.paoloambrosio.drizzle.core.action

import net.paoloambrosio.drizzle.core._

import scala.concurrent.{ExecutionContext, Future}

object TimedActionFactory {

  /**
    * Timed action, excluding postprocessing that is not part of it,
    * like checks and variable extraction.
    *
    * @tparam T internal output, for postprocessing
    */
  type TimedAction[T] = ScenarioContext => Future[(ScenarioContext, T)]

  /**
    * Part of the action that will be timed.
    *
    * This type allows the user to easily define actions without being
    * concerned with timers.
    *
    * @tparam T internal output, for postprocessing
    */
  type TimedPart[T] = SessionVariables => Future[(SessionVariables, T)]

  /**
    * Postprocessing step, not timed. Synchronous.
    *
    * @tparam T internal output, for further postprocessing
    */
  type NotTimedPart[T] = (ScenarioContext, T) => (ScenarioContext, T)
}

trait TimedActionFactory {
  import TimedActionFactory._

  implicit def ec: ExecutionContext

  def timedAction[T](f: TimedPart[T]): TimedAction[T]

  final implicit def toScenarioAction[T](f: TimedAction[T]): ScenarioAction =
    f.andThen(_.map(_._1))
}