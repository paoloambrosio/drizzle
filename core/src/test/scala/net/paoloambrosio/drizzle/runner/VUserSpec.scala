package net.paoloambrosio.drizzle.runner

import java.time.{Clock, Instant, OffsetDateTime, ZoneId}

import akka.actor.ActorSystem
import akka.testkit.{CallingThreadDispatcher, TestKit}
import com.miguno.akka.testing.VirtualTime
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.runner.VUser._
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._
import org.scalatest.concurrent.AsyncAssertions
import org.scalatest.{FlatSpecLike, Matchers}
import utils.CallingThreadExecutionContext

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class VUserSpec extends TestKit(ActorSystem("mock-scheduler")) with FlatSpecLike
    with Matchers with AsyncAssertions {

  it should "run actions sequentially" in new TestContext {
    val vuser = actorFor(fast(), fast(), fast())

    actionsStart shouldBe Seq.empty

    vuser ! Run

    w { actionsStart shouldBe Seq(runStart, runStart, runStart) }
  }

  it should "support asynchronous actions" in new TestContext {
    val vuser = actorFor(fast(), delayed(someDelay), fast())

    vuser ! Run

    time.advance(someDelay - smallDuration)
    w { actionsStart shouldBe Seq(runStart, runStart) }
    time.advance(smallDuration)
    w { actionsStart shouldBe Seq(runStart, runStart, someDelay) }
  }

  it should "pass new context to the next step" in new TestContext {
    val ic = ScenarioContext(ActionTimers(OffsetDateTime.now(clock), Duration.Zero))
    val f = incrementStartByASecond
    val vuser = actorFor(fast(f), fast(f))

    vuser ! Run

    w { contexts shouldBe Seq((ic, f(ic)),(f(ic),f(f(ic)))) }
  }

  // HELPERS

  trait TestContext extends ActionFactory with MockTime {
    override implicit val ec: ExecutionContext = ExecutionContext.global
    val w = new Waiter

    val clock: Clock = Clock.fixed(Instant.ofEpochSecond(1000), ZoneId.systemDefault())
    val runStart = time.elapsed
    val smallDuration = 1 milli
    lazy val someDelay = (new Random().nextInt(9)+1) seconds

    def actorFor(actions: ScenarioAction*) = {
      val steps = actions.map(ScenarioStep("step", _)).toStream
      system.actorOf(props(Scenario("scenario", steps), clock).withDispatcher(CallingThreadDispatcher.Id))
    }
  }

  trait ActionFactory { this: MockTime =>
    implicit val ec: ExecutionContext

    var actionsStart = Seq.empty[FiniteDuration]
    var contexts = Seq.empty[(ScenarioContext, ScenarioContext)]

    def fast(f: ScenarioContext => ScenarioContext = c => c) = recordContext { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      Future.successful(f(c))
    }}

    def delayed(d: FiniteDuration) = recordContext { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      akka.pattern.after(d, time.scheduler)(Future.successful(c))
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

  trait MockTime {
    val time = new VirtualTime
  }

}
