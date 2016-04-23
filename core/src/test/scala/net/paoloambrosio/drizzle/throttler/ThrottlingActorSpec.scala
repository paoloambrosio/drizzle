package net.paoloambrosio.drizzle.throttler

import java.time.{Clock, Duration}

import akka.actor.Scheduler
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import com.miguno.akka.testing.VirtualTime
import net.paoloambrosio.drizzle.throttler.ThrottlingActor._
import org.mockito.Mockito._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{CallingThreadExecutionContext, TestActorSystem}

import scala.concurrent.ExecutionContext

class ThrottlingActorSpec extends TestKit(TestActorSystem()) with ImplicitSender
  with FlatSpecLike with Matchers with BeforeAndAfterAll
  with ScalaFutures with MockitoSugar {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "respond immediately if throttling not needed" in new TestContext {
    when(clock.millis()).thenReturn(start, start + 100L)
    when(throttler.throttle(Duration.ofMillis(100))).thenReturn(Duration.ofMillis(100))

    throttlingActor ! ThrottlingRequest

    expectMsg(ThrottlingResponse)
  }

  it should "schedule response if throttling needed" in new TestContext {
    when(clock.millis()).thenReturn(start, start + 100L)
    when(throttler.throttle(Duration.ofMillis(100))).thenReturn(Duration.ofMillis(123))

    throttlingActor ! ThrottlingRequest

    expectNoMsg()
    time.advance(22)
    expectNoMsg()
    time.advance(1)
    expectMsg(ThrottlingResponse)
  }

  trait TestContext {
    val start = 123456L
    val clock: Clock = mock[Clock]
    val time = new VirtualTime

    val throttler = mock[Throttler]
    lazy val throttlingActor = TestActorRef(new ThrottlingActor(throttler, clock) {
      override val scheduler: Scheduler = time.scheduler
      override implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    })
  }
}
