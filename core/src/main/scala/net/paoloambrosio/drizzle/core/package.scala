package net.paoloambrosio.drizzle

import java.time.{Duration, OffsetDateTime}

import scala.concurrent.Future

package object core {

  case class ActionTimers(start: OffsetDateTime, elapsedTime: Duration)

  case class ScenarioContext(lastAction: ActionTimers)

  type ScenarioAction = ScenarioContext => Future[ScenarioContext]

  case class ScenarioStep(val name: String, val action: ScenarioAction)

  case class Scenario(val name: String, val steps: Stream[ScenarioStep])

  case class LoadProfile(val scenario: Scenario, val loadInjectionSteps: Stream[Duration])

}
