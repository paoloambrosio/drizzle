package net.paoloambrosio.drizzle.runner

import java.time.Clock

import akka.actor.{Actor, ActorRef, Props}
import akka.testkit.{ImplicitSender, TestActorRef, TestKit}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.events.VUserEventSource
import net.paoloambrosio.drizzle.runner.Orchestrator._
import org.scalatest.mock.MockitoSugar
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem
import org.mockito.Mockito._

class OrchestratorSpec extends TestKit(TestActorSystem()) with ImplicitSender
    with FlatSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {

  override def afterAll {
    super.afterAll
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

  it should "send vuser created and terminated events" in new TestContext {
    val orchestrator = actor()

    orchestrator ! Start(Seq(SomeScenario()))

    verify(vUserEventSource, times(1)).fireVUserCreated()
    verifyNoMoreInteractions(vUserEventSource)
    reset(vUserEventSource)

    orchestrator.tell(VUser.Success, startedVusers.head.ref)

    verify(vUserEventSource, times(1)).fireVUserTerminated()
    verifyNoMoreInteractions(vUserEventSource)
  }

  // HELPERS

  trait TestContext {

    def SomeScenario(sa: ScenarioAction*) = Scenario("", sa.map(ScenarioStep(None, _)).toStream)

    val vUserEventSource = mock[VUserEventSource]

    def actor() = {
      TestActorRef(new Orchestrator(Clock.systemUTC(), testVUserProps, vUserEventSource))
    }

    // VUSERS

    val testVUserProps = Props(new Actor {
      override def receive: Receive = {
        case VUser.Start(steps) =>
          startedVusers += StartedVUser(self, steps)
      }
    })

    case class StartedVUser(ref: ActorRef, steps: Stream[ScenarioStep])

    var startedVusers = Set.empty[StartedVUser]
    def startedVuserRefs = startedVusers.map(_.ref)
    def splitVuserRefs() = startedVuserRefs.splitAt(startedVusers.size/2)
  }

}