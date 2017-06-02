package net.paoloambrosio.drizzle.core.action

import net.paoloambrosio.drizzle.core._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

object TimedActionFactory {

  /**
    * Preprocessing step, not timed. Synchronous.
    *
    * @tparam T timed part input
    */
  type PreTimedPart[I] = ScenarioContext => I

  /**
    * Part of the action that will be timed.
    *
    * This type allows the user to easily define actions without being
    * concerned with timers.
    *
    * @tparam T internal output, for postprocessing
    */
  type TimedPart[I,O] = I => Future[O]

  /**
    * Postprocessing step, not timed for further postprocessing. Synchronous.
    *
    * @tparam T timed part output
    */
  type PostTimedPart[O] = (ScenarioContext, O) => Try[(ScenarioContext, O)]
}

trait TimedActionFactory {
  import TimedActionFactory._

  implicit def ec: ExecutionContext

  final def timedAction[I,O](pre: PreTimedPart[I], f: TimedPart[I,O], post: PostTimedPart[O]): ScenarioAction = {
    sc: ScenarioContext => {
      pre.andThen(timed(f))(sc).flatMap {
        case (timers, output) => Future.fromTry(
          post(sc.copy(
            latestAction = sc.latestAction.copy(timers = Some(timers))
          ), output).map(_._1)
        )
      }
    }
  }

  def timed[I,O](f: TimedPart[I,O]): TimedPart[I,(ActionTimers, O)]
}