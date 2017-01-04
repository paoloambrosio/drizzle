package net.paoloambrosio.drizzle

import java.time.{Duration, OffsetDateTime}

import net.paoloambrosio.drizzle.core.expression.Expression

import scala.concurrent.Future

package object core {

  type SessionVariables = Map[String, Any]

  case class ActionTimers(start: OffsetDateTime, elapsedTime: Duration)

  case class ScenarioContext(latestAction: Option[ActionTimers] = None, sessionVariables: SessionVariables = Map.empty)

  type ScenarioAction = ScenarioContext => Future[ScenarioContext]

  case class ScenarioStep(name: Option[Expression[String]], action: ScenarioAction)

  case class Scenario(name: String, steps: Stream[ScenarioStep])

  case class ScenarioProfile(scenario: Scenario, loadProfile: Seq[Duration])

}
