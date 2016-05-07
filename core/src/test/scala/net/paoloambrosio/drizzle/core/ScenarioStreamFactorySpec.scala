package net.paoloambrosio.drizzle.core

import java.time.{OffsetDateTime, Duration => jDuration}

import net.paoloambrosio.drizzle.core.action.SleepActionFactory
import net.paoloambrosio.drizzle.core.events.VUserEventSource
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{FlatSpec, Matchers}
import utils.CallingThreadExecutionContext

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}

class ScenarioStreamFactorySpec extends FlatSpec with Matchers with MockitoSugar {

  it should "delay vusers based on the load profile" in new TestContext {
    scenarioStream(Seq(
        ScenarioProfile(emptyScenario, Seq(1 second, 2 seconds, 3 seconds))
    ))

    initialDelays shouldBe Seq(1 second, 3 seconds, 6 seconds)
  }

  it should "delay vusers separately for each scenario profile" in new TestContext {
    scenarioStream(Seq(
      ScenarioProfile(emptyScenario, Seq(1 second, 2 seconds)),
      ScenarioProfile(emptyScenario, Seq(3 seconds))
    ))

    initialDelays shouldBe Seq(1 second, 3 seconds, 3 seconds)
  }

  it should "send vuser started event" in new TestContext {
    val steps = producedSteps(emptyScenario)

    // one artificial step added to the chain
    steps.size shouldBe emptyScenario.steps.size + 1
    verifyNoMoreInteractions(vUserEventSource)

    // call the artificial step
    steps.head.action(ScenarioContext()).value

    verify(vUserEventSource, times(1)).fireVUserStarted()
  }

  it should "send vuser action metrics event" in new TestContext {
    val start = OffsetDateTime.MIN
    val steps = producedSteps(aScenario(
      outputTimers(start, 1 milli),
      outputTimers(start.plusSeconds(4), 2 millis),
      eraseTimers(),
      outputTimers(start.plusSeconds(7), 3 millis)
    ))
    verifyNoMoreInteractions(vUserEventSource)

    // execute steps skipping the artificial step
    executeStepChain(steps.tail)

    val ordered = Mockito.inOrder(vUserEventSource)
    ordered.verify(vUserEventSource).fireVUserMetrics(ActionTimers(start, 1 milli))
    ordered.verify(vUserEventSource).fireVUserMetrics(ActionTimers(start.plusSeconds(4), 2 milli))
    ordered.verify(vUserEventSource).fireVUserMetrics(ActionTimers(start.plusSeconds(7), 3 milli))
  }

  // HELPERS

  trait TestContext extends ScenarioStreamFactory with SleepActionFactory {

    override val vUserEventSource: VUserEventSource = mock[VUserEventSource]
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext

    lazy val emptyScenario = aScenario()
    def aScenario(sa: ScenarioAction*) = Scenario("", sa.map(ScenarioStep(None, _)).toStream)

    def producedSteps(scenario: Scenario) = scenarioStream(Seq(
      ScenarioProfile(scenario, Seq(1 second))
    )).head.steps

    // INITIAL DELAY

    var initialDelays = Seq.empty[Duration]

    private def recordStartDelay(startDelay: Duration) = {
      initialDelays = initialDelays :+ startDelay
    }

    /* This allows the test to record initial durations */
    override def thinkTime(duration: jDuration) = {
      recordStartDelay(duration)
      sc => Future.successful(sc)
    }

    override def pacing(duration: jDuration) = ???

    // ACTION METRICS

    def outputTimers(start: OffsetDateTime, elapsedTime: Duration): ScenarioAction = sc => Future.successful(
      ScenarioContext(Some(ActionTimers(start, elapsedTime)))
    )

    def eraseTimers(): ScenarioAction = sc => Future.successful(
      ScenarioContext(None)
    )

    def executeStepChain(steps: Stream[ScenarioStep]) = {
      steps.foreach { ss => ss.action(ScenarioContext()).value.get.get } // A bit nasty...
    }
  }

}
