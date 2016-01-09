package net.paoloambrosio.drizzle

import java.time.{Duration, OffsetDateTime}

import scala.concurrent.Future

package object core {

  type SessionVariables = Unit

  case class ActionTimers(start: OffsetDateTime, elapsedTime: Duration)

  case class ScenarioContext(lastAction: ActionTimers, sessionVariables: SessionVariables = ())

  type ScenarioAction = ScenarioContext => Future[ScenarioContext]

  case class ScenarioStep(val name: String, val action: ScenarioAction)

  case class Scenario(val name: String, val steps: Stream[ScenarioStep])

  type LoadProfile = Stream[Duration]

}
