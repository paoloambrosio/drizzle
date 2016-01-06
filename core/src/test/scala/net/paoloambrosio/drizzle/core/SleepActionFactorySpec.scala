package net.paoloambrosio.drizzle.core

import java.time.OffsetDateTime

import akka.actor.Scheduler
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import com.miguno.akka.testing.VirtualTime
import org.scalatest.{Matchers, FlatSpec}
import utils.CallingThreadExecutionContext

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import scala.util.{Random, Success}

class SleepActionFactorySpec extends FlatSpec with Matchers {

  "thinkTime" should "delay next step from call" in new TestContext {
    val passedContext = contextWithElapsedTime(someDuration / 2)
    val expectedDelay = someDuration

    val action = thinkTime(someDuration)
    val ret = action(passedContext)

    ret.value shouldBe None
    time.advance(expectedDelay - smallDuration)
    ret.value shouldBe None
    time.advance(smallDuration)
    ret.value shouldBe Some(Success(passedContext))
  }

  "pacing" should "delay next step from start of previous action" in new TestContext {
    val passedContext = contextWithElapsedTime(someDuration / 2)
    val expectedDelay = someDuration - passedContext.lastAction.elapsedTime

    val action = pacing(someDuration)
    val ret = action(passedContext)

    ret.value shouldBe None
    time.advance(expectedDelay - smallDuration)
    ret.value shouldBe None
    time.advance(smallDuration)
    ret.value shouldBe Some(Success(passedContext))
  }

  it should "not delay next step if previous action took longer than pacing" in new TestContext {
    val action = pacing(someDuration)
    val context = contextWithElapsedTime(someDuration)
    val ret = action(context)

    ret.value shouldBe Some(Success(context))
  }

  // HELPERS

  trait TestContext extends SleepActionFactory {
    implicit val time = new VirtualTime
    implicit val scheduler: Scheduler = time.scheduler
    implicit val ec: ExecutionContext = new CallingThreadExecutionContext

    val someDuration = (new Random().nextInt(9)+1) seconds
    val smallDuration = 1 milli

    val actionStart = OffsetDateTime.now()

    def contextWithElapsedTime(elapsedTime: Duration) = {
      ScenarioContext(ActionTimers(actionStart, elapsedTime))
    }
  }

}
