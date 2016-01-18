package net.paoloambrosio.drizzle.core

import java.time.OffsetDateTime

import net.paoloambrosio.drizzle.core.action.SleepActionFactory
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.Future

class ScenarioStreamFactorySpec extends FlatSpec with Matchers {

  it should "delay vusers based on the load profile" in new TestContext {
    val ss = scenarioStream(Seq(
        ScenarioProfile(emptyScenario, Seq(1 second, 2 seconds, 3 seconds))
    ))

    initialDelays(ss) shouldBe Seq(1 second, 3 seconds, 6 seconds)
  }

  it should "delay vusers separately for each scenario profile" in new TestContext {
    val ss = scenarioStream(Seq(
      ScenarioProfile(emptyScenario, Seq(1 second, 2 seconds)),
      ScenarioProfile(emptyScenario, Seq(3 seconds))
    ))

    initialDelays(ss) shouldBe Seq(1 second, 3 seconds, 3 seconds)
  }

  // HELPERS

  trait TestContext extends ScenarioStreamFactory with SleepActionFactory {

    val emptyScenario = Scenario("", Stream.empty[ScenarioStep])
    val initialContext = ScenarioContext(ActionTimers(OffsetDateTime.now(), Duration.Zero))

    def initialDelays(scenarios: Seq[Scenario]): Seq[FiniteDuration] = {
      scenarios.flatMap(_.steps).map(s => extractDuration(s.action))
    }

    def extractDuration(action: ScenarioAction): FiniteDuration = {
      action(initialContext).value.get.get.lastAction.elapsedTime
    }

    /* This allows the test to record initial durations */
    override def thinkTime(duration: java.time.Duration): ScenarioAction = { sc: ScenarioContext =>
      Future.successful(sc.copy(lastAction = ActionTimers(sc.lastAction.start, duration)))
    }

    override def pacing(duration: java.time.Duration): ScenarioAction = ???
  }

}
