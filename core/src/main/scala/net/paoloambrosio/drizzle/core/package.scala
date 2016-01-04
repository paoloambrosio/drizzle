package net.paoloambrosio.drizzle

import java.time.{Duration, OffsetDateTime}

import scala.concurrent.Future

package object core {

  case class ActionTimers(start: OffsetDateTime, elapsedTime: Duration)

  case class ScenarioContext(lastAction: ActionTimers)

  type ScenarioAction = ScenarioContext => Future[ScenarioContext]
  object ScenarioActionFactory extends SleepActionFactory

  class ScenarioStep(val name: String, val exec: ScenarioAction)

  class Scenario(val name: String, val steps: Seq[ScenarioStep])
}
