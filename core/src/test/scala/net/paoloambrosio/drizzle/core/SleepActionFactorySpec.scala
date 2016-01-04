package net.paoloambrosio.drizzle.core

import java.time.OffsetDateTime

import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import com.miguno.akka.testing.VirtualTime
import org.scalatest.{Matchers, FlatSpec}

import scala.concurrent.Future
import scala.concurrent.duration._
import scala.util.{Random, Success}

class SleepActionFactorySpec extends FlatSpec with Matchers {

  val actionStart = OffsetDateTime.now()
  val someDuration = new Random().nextInt(10) seconds

  def contextWithElapsedTime(elapsedTime: Duration) = ScenarioContext(ActionTimers(actionStart, elapsedTime))

  trait MockScheduler {
    implicit val time = new VirtualTime
    implicit val scheduler = time.scheduler
    implicit val ec = scala.concurrent.ExecutionContext.global
  }

  trait TestContext extends SleepActionFactory with MockScheduler

  "sleep" should "delay next step from call" in new TestContext {
    val action = sleep(someDuration)
    val context = contextWithElapsedTime(someDuration / 2)
    val ret = action(context)

    val expectedDelay = someDuration

    shouldHaveValueAfter(ret, context, expectedDelay)
  }

  "pacing" should "delay next step from start of previous action" in new TestContext {
    val action = pacing(someDuration)
    val context = contextWithElapsedTime(someDuration / 2)
    val ret = action(context)

    val expectedDelay = someDuration - context.lastAction.elapsedTime

    shouldHaveValueAfter(ret, context, expectedDelay)
  }

  it should "not delay next step if previous action took longer than pacing" in new TestContext {
    val action = pacing(someDuration)
    val context = contextWithElapsedTime(someDuration)
    val ret = action(context)

    ret.value shouldBe Some(Success(context))
  }

  // HELPERS

  /*
   * Verifies that a future is ready only after the expected delay.
   * TODO: Implement it with matchers (e.g. futureValue should beReadyAfter expectedDelay withValue expectedValue)
   */
  private def shouldHaveValueAfter[T](futureValue: Future[T], expectedValue: T, expectedDelay: FiniteDuration)(implicit time: VirtualTime) = {
    futureValue.value shouldBe None
    time.advance(expectedDelay.toMillis - 1)
    futureValue.value shouldBe None
    time.advance(1)
    futureValue.value shouldBe Some(Success(expectedValue))
  }

}
