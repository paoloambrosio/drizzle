package net.paoloambrosio.drizzle.throttler

import java.time.{Clock, Duration}

import akka.testkit.{DefaultTimeout, ImplicitSender, TestKit, TestProbe}
import net.paoloambrosio.drizzle.core.{ScenarioAction, ScenarioContext}
import net.paoloambrosio.drizzle.throttler.ThrottlingActor._
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{CallingThreadExecutionContext, TestActorSystem}

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.FiniteDuration

class AkkaActorThrottledActionFactorySpec extends TestKit(TestActorSystem())
  with ImplicitSender with DefaultTimeout
  with FlatSpecLike with Matchers with BeforeAndAfterAll
  with ScalaFutures with MockitoSugar {

  override def afterAll {
    super.afterAll
    TestKit.shutdownActorSystem(system)
  }

  it should "wait for throttling actor to reply" in new TestContext {
    val innerAction: ScenarioAction = { ctx => Future.successful(ctx) }
    val throttledAction = throttle(innerAction)
    probe.expectNoMsg()

    val result = throttledAction(ScenarioContext())
    probe.expectMsg(ThrottlingRequest)
    result.isCompleted shouldBe false

    probe.reply(ThrottlingResponse)
    result.isCompleted shouldBe true
  }


  trait TestContext extends AkkaActorThrottledActionFactory {
    val probe = TestProbe()
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    override def throttlerTimeout: FiniteDuration = timeout.duration

    override def throttlingActor = probe.ref
  }
}
