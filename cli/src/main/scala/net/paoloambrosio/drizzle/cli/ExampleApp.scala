package net.paoloambrosio.drizzle.cli

import net.paoloambrosio.drizzle.core.{Scenario, ScenarioProfile, ScenarioStep}
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.Future
import scala.concurrent.duration._

object ExampleApp extends DrizzleApp {

  override def simulation = Seq(
    ScenarioProfile(Scenario("A", Stream(
      printScenario("A1"), sleepScenario(2 seconds), printScenario("A2")
    )), rampUsers(4, 1 second)),
    ScenarioProfile(Scenario("B", Stream(
      printScenario("B1"), sleepScenario(2 seconds), printScenario("B2")
    )), rampUsers(4, 2 seconds))
  )

  def printScenario(s: String) = ScenarioStep(Some("print"), timedAction(sc => { print(s); Future.successful(sc) }))
  def sleepScenario(t: FiniteDuration) = ScenarioStep(Some("wait"), thinkTime(t))

  def rampUsers(n: Int, over: java.time.Duration) = Stream.continually(over.dividedBy(n)).take(n)
}
