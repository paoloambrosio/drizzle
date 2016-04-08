package net.paoloambrosio.drizzle.runner

import java.time.{Clock, OffsetDateTime, Duration => jDuration}

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit, TestProbe}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.metrics.TimedActionMetrics
import net.paoloambrosio.drizzle.runner.Orchestrator._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.Future
import scala.concurrent.duration._

class OrchestratorSpec extends TestKit(TestActorSystem()) with ImplicitSender
    with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "finish immediately if there is nothing to do" in new TestContext {
    val orchestrator = actor()

    orchestrator ! Start(Seq.empty[Scenario])

    startedVusers shouldBe empty
    expectMsg(Finished)
  }

  it should "start a vuser for each passed scenario" in new TestContext {
    val orchestrator = actor()
    val scenarios = Seq.fill(3)(SomeScenario())

    orchestrator ! Start(scenarios)

    startedVusers.size shouldBe scenarios.size // one vuser per scenario
    expectNoMsg()
  }

  it should "finish when all vusers exit" in new TestContext {
    val orchestrator = actor()
    val scenarios = Seq.fill(3)(SomeScenario())

    orchestrator ! Start(scenarios)

    val (half, theOtherHalf) = splitVuserRefs()
    expectNoMsg()
    half.foreach(orchestrator.tell(VUser.Success, _))
    expectNoMsg()
    theOtherHalf.foreach(orchestrator.tell(VUser.Failure(new Exception("BOOM!")), _))
    expectMsg(Finished)
  }

  it should "pass metrics to collectors when actions are run in vusers" in new TestContext {
    val collectors = Seq(TestProbe(), TestProbe(), TestProbe())
    val orchestrator = actor(collectors.map(_.ref))
    val start = OffsetDateTime.now()
    val scenario = Seq(SomeScenario(
      outputTimers(start, 1 milli),
      outputTimers(start.plusSeconds(4), 2 millis),
      eraseTimers(),
      outputTimers(start.plusSeconds(7), 3 millis)
    ))

    orchestrator ! Start(scenario)

    executeStepChain(startedVusers.head)
    collectors.foreach { mc => {
      expectExportedMetrics(mc, start, 1 milli)
      expectExportedMetrics(mc, start.plusSeconds(4), 2 milli)
      expectExportedMetrics(mc, start.plusSeconds(7), 3 milli)
    }}
  }

  // HELPERS

  trait TestContext {
    def SomeScenario(sa: ScenarioAction*) = Scenario("", sa.map(ScenarioStep(None, _)).toStream)

    def outputTimers(start: OffsetDateTime, elapsedTime: Duration): ScenarioAction = sc => Future.successful(
      ScenarioContext(Some(ActionTimers(start, elapsedTime)))
    )

    def eraseTimers(): ScenarioAction = sc => Future.successful(
      ScenarioContext(None)
    )

    case class StartedVUser(ref: ActorRef, steps: Stream[ScenarioStep])
    var startedVusers = Set.empty[StartedVUser]

    def splitVuserRefs() = startedVusers.map(_.ref).splitAt(startedVusers.size/2)

    val testVUserProps = Props(new Actor {
      override def receive: Receive = {
        case VUser.Start(steps) =>
          startedVusers += StartedVUser(self, steps)
      }
    })

    def actor(metricsCollectors: Seq[ActorRef] = Seq.empty) = {
      TestActorRef(new Orchestrator(Clock.systemUTC(), metricsCollectors, testVUserProps))
    }

    def executeStepChain(vu: StartedVUser) = {
      vu.steps.foreach { ss => ss.action(ScenarioContext()).value.get.get } // FIXME
    }

    def expectExportedMetrics(testProbe: TestProbe, start: OffsetDateTime, elapsedTime: jDuration) = {
      testProbe.expectMsgPF() {
        case msg: TimedActionMetrics =>
          msg.start shouldBe start
          msg.elapsedTime shouldBe elapsedTime
      }
    }
  }

}
