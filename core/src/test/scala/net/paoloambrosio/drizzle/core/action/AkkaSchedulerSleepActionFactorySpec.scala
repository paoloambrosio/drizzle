package net.paoloambrosio.drizzle.core.action

import java.time.OffsetDateTime

import akka.actor.Scheduler
import com.miguno.akka.testing.VirtualTime
import net.paoloambrosio.drizzle.core.{ActionResult, ActionTimers, ScenarioContext}
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import org.scalatest.{FlatSpec, Matchers}
import utils.CallingThreadExecutionContext

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._
import scala.util.{Random, Success}

class AkkaSchedulerSleepActionFactorySpec extends FlatSpec with Matchers {

  "thinkTime" should "delay next step from call" in new TestContext {
    val passedContext = contextWithElapsedTime(someDuration / 2)
    val expectedDelay = someDuration

    val action = thinkTime(someDuration)
    val ret = action(passedContext)

    ret.value shouldBe None
    time.advance(expectedDelay - smallDuration)
    ret.value shouldBe None
    time.advance(smallDuration)
    ret.value shouldBe Some(Success(contextWithNoTimers))
  }

  "pacing" should "delay next step from start of previous action" in new TestContext {
    val elapsedTime = someDuration / 2
    val passedContext = contextWithElapsedTime(someDuration / 2)
    val expectedDelay = someDuration - elapsedTime

    val action = pacing(someDuration)
    val ret = action(passedContext)

    ret.value shouldBe None
    time.advance(expectedDelay - smallDuration)
    ret.value shouldBe None
    time.advance(smallDuration)
    ret.value shouldBe Some(Success(contextWithNoTimers))
  }

  it should "not delay next step if previous action took longer than pacing" in new TestContext {
    val action = pacing(someDuration)
    val passedContext = contextWithElapsedTime(someDuration)
    val ret = action(passedContext)

    ret.value shouldBe Some(Success(contextWithNoTimers))
  }

  it should "not delay next step if previous action has no timers" in new TestContext {
    val action = pacing(someDuration)
    val ret = action(contextWithNoTimers)

    ret.value shouldBe Some(Success(contextWithNoTimers))
  }


  // HELPERS

  trait TestContext extends AkkaSchedulerSleepActionFactory {
    val time = new VirtualTime
    override val scheduler: Scheduler = time.scheduler
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext

    val someDuration = (new Random().nextInt(9)+1) seconds
    val smallDuration = 1 milli

    val actionStart = OffsetDateTime.now()

    def contextWithElapsedTime(elapsedTime: Duration) = {
      ScenarioContext(ActionResult(timers = Some(ActionTimers(actionStart, elapsedTime))))
    }

    val contextWithNoTimers = ScenarioContext()
  }

}
