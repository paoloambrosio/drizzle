package net.paoloambrosio.drizzle.core

import java.time.{Duration => jDuration}

import net.paoloambrosio.drizzle.core.action.SleepActionFactory
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import org.scalatest.{FlatSpec, Matchers}

import scala.concurrent.duration._
import scala.concurrent.Future

class ScenarioStreamFactorySpec extends FlatSpec with Matchers {

  it should "delay vusers based on the load profile" in new TestContext {
    scenarioStream(Seq(
        ScenarioProfile(anyScenario, Seq(1 second, 2 seconds, 3 seconds))
    ))

    initialDelays shouldBe Seq(1 second, 3 seconds, 6 seconds)
  }

  it should "delay vusers separately for each scenario profile" in new TestContext {
    scenarioStream(Seq(
      ScenarioProfile(anyScenario, Seq(1 second, 2 seconds)),
      ScenarioProfile(anyScenario, Seq(3 seconds))
    ))

    initialDelays shouldBe Seq(1 second, 3 seconds, 3 seconds)
  }

  // HELPERS

  trait TestContext extends ScenarioStreamFactory with SleepActionFactory {

    val anyScenario = Scenario("", Stream.empty[ScenarioStep])

    var initialDelays = Seq.empty[Duration]

    /* This allows the test to record initial durations */
    override def thinkTime(duration: jDuration) = {
      recordStartDelay(duration)
      sc => Future.successful(sc)
    }

    override def pacing(duration: jDuration) = ???

    private def recordStartDelay(startDelay: Duration) = {
      initialDelays = initialDelays :+ startDelay
    }
  }

}
