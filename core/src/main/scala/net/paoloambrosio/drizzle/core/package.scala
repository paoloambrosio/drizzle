package net.paoloambrosio.drizzle

import java.time.{Duration, OffsetDateTime}

import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.utils.CDStream

import scala.concurrent.Future

package object core {

  type SessionVariables = Map[String, Any]
  val SessionVariables = Map

  case class ActionTimers(start: OffsetDateTime, elapsedTime: Duration)
  case class ActionResult(timers: Option[ActionTimers] = None, error: Option[Exception] = None)

  case class ScenarioContext(latestAction: ActionResult = ActionResult(), sessionVariables: SessionVariables = SessionVariables.empty)

  type ScenarioAction = ScenarioContext => Future[ScenarioContext] //

  sealed trait ScenarioStep
  case class ActionStep(name: Option[Expression[String]], action: ScenarioAction) extends ScenarioStep
  case class LoopStep(condition: ScenarioContext => (ScenarioContext, Boolean), body: Seq[ScenarioStep]) extends ScenarioStep
  case class ConditionalStep(condition: ScenarioContext => Boolean, body: Seq[ScenarioStep]) extends ScenarioStep

  case class ActionExecutor(name: Option[String], action: () => Future[ScenarioContext])

  type StepStream = CDStream[ScenarioContext, ActionExecutor]
  val StepStream = CDStream

  case class Scenario(name: String, steps: Seq[ScenarioStep])

  case class ScenarioProfile(scenario: Scenario, loadProfile: Seq[Duration])

}
