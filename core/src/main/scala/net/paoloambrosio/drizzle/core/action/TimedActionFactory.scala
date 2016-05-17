package net.paoloambrosio.drizzle.core.action

import net.paoloambrosio.drizzle.core._

import scala.concurrent.{ExecutionContext, Future}

object TimedActionFactory {
  type TimedPart[T] = SessionVariables => Future[(SessionVariables, T)]
  type TimedAction[T] = ScenarioContext => Future[(ScenarioContext, T)]
}

trait TimedActionFactory {
  import TimedActionFactory._

  implicit def ec: ExecutionContext

  def timedAction[T](f: TimedPart[T]): TimedAction[T]

  final implicit def toScenarioAction[T](f: TimedAction[T]): ScenarioAction =
    f.andThen(_.map(_._1))
}