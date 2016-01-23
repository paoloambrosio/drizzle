package net.paoloambrosio.drizzle.runner

import java.time.{Clock, Instant, OffsetDateTime, ZoneId}

import akka.actor.ActorSystem
import akka.testkit.{ImplicitSender, TestFSMRef, TestKit}
import com.miguno.akka.testing.VirtualTime
import com.typesafe.config.ConfigFactory
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.runner.VUser._
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import org.scalatest.concurrent.Eventually
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{TestActorSystem, CallingThreadExecutionContext}

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class VUserSpec extends TestKit(TestActorSystem()) with ImplicitSender
    with FlatSpecLike with Matchers with BeforeAndAfterAll with Eventually {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "run actions sequentially" in new TestContext {
    actionsStart shouldBe Seq.empty

    vuser ! Start(scenario(fast(), delayed(someDelay), fast()))

    expectMsg(VUser.Success)
    actionsStart shouldBe Seq(runStart, runStart, someDelay)
  }

  it should "pass new context to the next step" in new TestContext {
    val ic = ScenarioContext(ActionTimers(OffsetDateTime.now(clock), Duration.Zero))
    val f = incrementStartByASecond

    vuser ! Start(scenario(fast(f), fast(f)))

    expectMsg(VUser.Success)
    contexts shouldBe Seq((ic, f(ic)),(f(ic),f(f(ic))))
  }

  it should "stop on exceptions in actions" in new TestContext {
    val exception = new Exception("BOOM!")

    vuser ! Start(scenario(failing(exception), fast()))

    expectMsg(VUser.Failure(exception))
    actionsStart shouldBe Seq(runStart)
  }

  // HELPERS

  trait TestContext extends ActionFactory {
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    val clock: Clock = Clock.fixed(Instant.ofEpochSecond(1000), ZoneId.systemDefault())

    val time = new VirtualTime
    val runStart = time.elapsed

    lazy val vuser = TestFSMRef(new VUser(clock), testActor)

    lazy val someDelay = (new Random().nextInt(9)+1) seconds

    def scenario(actions: ScenarioAction*) = {
      val steps = actions.map(ScenarioStep("step", _)).toStream
      Scenario("scenario", steps)
    }
  }

  // TODO remove virtual timer here!
  trait ActionFactory {
    implicit def ec: ExecutionContext
    def time: VirtualTime

    var actionsStart = Seq.empty[FiniteDuration]
    var contexts = Seq.empty[(ScenarioContext, ScenarioContext)]

    def fast(f: ScenarioContext => ScenarioContext = c => c) = recordContext { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      Future.successful(f(c))
    }}

    def delayed(d: FiniteDuration) = recordContext { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      time.advance(d)
      Future.successful(c)
    }}

    def failing(e: Exception) = recordContext { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      Future.failed(e)
    }}

    private def recordContext(action: ScenarioAction): ScenarioAction = { sc: ScenarioContext =>
      action(sc).map(ec => {
        contexts :+= (sc, ec)
        ec
      })
    }

    val incrementStartByASecond: ScenarioContext => ScenarioContext = { c =>
      // It is worth introducing a lens library?
      c.copy(lastAction = c.lastAction.copy(start = c.lastAction.start.plusSeconds(1)))
    }
  }

}
