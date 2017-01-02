package net.paoloambrosio.drizzle.core.action

import java.time._

import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory.TimedPart
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import utils.CallingThreadExecutionContext

import scala.concurrent.{ExecutionContext, Future}

class JavaTimeTimedActionFactorySpec extends FlatSpec
    with Matchers with ScalaFutures {

  "timed" should "record start and elapsed times" in new TestContext {
    val action: TimedPart[Int, String] = i => Future.successful(i.toString)

    val (timers, value) = timed(action)(1).futureValue

    timers.start shouldBe OffsetDateTime.now(t1clock)
    timers.elapsedTime.toNanos should be > 0L
    timers.elapsedTime.toMillis should be < (patienceConfig.timeout.millisPart)

    value shouldBe "1"
  }

  // HELPERS

  trait TestContext extends JavaTimeTimedActionFactory {
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    override lazy val clock = t1clock

    private val t0clock: Clock = Clock.fixed(Instant.ofEpochSecond(1000), ZoneId.systemDefault())
    val t1clock: Clock = Clock.offset(t0clock, Duration.ofSeconds(2))

    def passedContext = Some(ActionTimers(OffsetDateTime.now(t0clock), Duration.ZERO))
  }

}
