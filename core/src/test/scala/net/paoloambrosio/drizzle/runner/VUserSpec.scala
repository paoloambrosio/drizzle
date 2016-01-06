package net.paoloambrosio.drizzle.runner

import java.time.Clock

import akka.actor.ActorSystem
import akka.testkit.{CallingThreadDispatcher, TestKit}
import com.miguno.akka.testing.VirtualTime
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.runner.VUser._
import org.scalatest.{FlatSpecLike, Matchers}
import utils.CallingThreadExecutionContext

import scala.concurrent.duration._
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Random

class VUserSpec extends TestKit(ActorSystem("mock-scheduler")) with FlatSpecLike with Matchers {

  it should "run actions sequentially" in new TestContext {
    val vuser = actorFor(fast(), fast(), fast())

    actionsStart shouldBe Seq.empty

    vuser ! Run

    actionsStart shouldBe Seq(runStart, runStart, runStart)
  }

  it should "support asynchronous actions" in new TestContext {
    val vuser = actorFor(fast(), delayed(someDelay), fast())

    vuser ! Run

    time.advance(someDelay - smallDuration)
    actionsStart shouldBe Seq(runStart, runStart)
    time.advance(smallDuration)
    actionsStart shouldBe Seq(runStart, runStart, someDelay)
  }

//  it should "change context" in {
//  }

  // HELPERS

  trait TestContext extends ActionFactory with MockTime {
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    val clock: Clock = Clock.systemUTC() // TODO needs a test to verify initial context

    val runStart = time.elapsed

    val smallDuration = 1 milli
    lazy val someDelay = (new Random().nextInt(9)+1) seconds

    def actorFor(actions: ScenarioAction*) = {
      val steps = actions.map(ScenarioStep("step", _)).toStream
      system.actorOf(props(Scenario("scenario", steps))(clock).withDispatcher(CallingThreadDispatcher.Id))
    }
  }

  trait ActionFactory { this: MockTime =>
    implicit val ec: ExecutionContext

    var actionsStart = Seq.empty[FiniteDuration]

    def fast() = { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      Future.successful(c)
    }}

    def delayed(d: FiniteDuration) = { c: ScenarioContext => {
      actionsStart :+= time.elapsed
      akka.pattern.after(d, time.scheduler)(Future.successful(c))
    }}
  }

  trait MockTime {
    val time = new VirtualTime
  }

}
