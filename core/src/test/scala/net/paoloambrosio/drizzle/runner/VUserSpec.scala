package net.paoloambrosio.drizzle.runner

import java.time._

import akka.actor.Actor
import akka.testkit._
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.metrics.TimedActionMetrics
import net.paoloambrosio.drizzle.runner.VUser._
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{CallingThreadExecutionContext, TestActorSystem}

import scala.concurrent.{ExecutionContext, Future, Promise}

class VUserSpec extends TestKit(TestActorSystem()) with ImplicitSender
    with FlatSpecLike with Matchers with BeforeAndAfterAll {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "pass new context to the next step" in new TestContext {
    val ic = initialContext
    val f = incrementStartByASecond

    vuser ! Start(scenario(successful(f), successful(f)))

    expectMsg(VUser.Success)
    // TODO check VUser terminated
    contexts shouldBe Seq((ic, f(ic)),(f(ic),f(f(ic))))
  }

  it should "stop on exceptions in actions" in new TestContext {
    val ic = initialContext
    val f = incrementStartByASecond
    val exception = new Exception("BOOM!")

    vuser ! Start(scenario(successful(f), failing(exception), successful()))

    expectMsg(VUser.Failure(exception))
    // TODO check VUser terminated
    contexts shouldBe Seq((ic, f(ic)))
  }

  it should "stop when requested" in new TestContext {
    vuser ! Start(scenario(async(), async()))

    actionsExecuted shouldBe 0
    advance()
    vuser ! Stop

    expectMsg(VUser.Success)
    // TODO check VUser terminated
    actionsExecuted shouldBe 1
  }

  it should "run async actions sequentially" in new TestContext {
    vuser ! Start(scenario(async(), async()))

    actionsExecuted shouldBe 0
    advance()
    actionsExecuted shouldBe 1
    advance()
    expectMsg(VUser.Success)
    actionsExecuted shouldBe 2
  }

  it should "send metrics if step name present" in new TestContext {
    vuser ! Start(Scenario("sc1", Stream(
      ScenarioStep(None, successful(changeTimers(elapsedTime = Duration.ofMillis(123)))),
      ScenarioStep(Some("st2"), successful(changeTimers(elapsedTime = Duration.ofNanos(456)))),
      ScenarioStep(None, successful(changeTimers(elapsedTime = Duration.ofNanos(789))))
    )))

    metricsSent.map(m => (m.start, m.elapsedTime)) shouldBe Seq(
      (testStartTime, Duration.ofNanos(456))
    )
  }

  // HELPERS

  trait TestContext {
    implicit val ec: ExecutionContext = new CallingThreadExecutionContext

    lazy val vuser = TestFSMRef(new VUser(clock, metricsRecorder), testActor)

    def scenario(actions: ScenarioAction*) = {
      val steps = actions.map(ScenarioStep(None, _)).toStream
      Scenario("scenario", steps)
    }

    val clock: Clock = Clock.fixed(Instant.ofEpochSecond(1000), ZoneId.systemDefault())

    val testStartTime = OffsetDateTime.now(clock)
    val initialContext = ScenarioContext(ActionTimers(testStartTime, Duration.ZERO))
    val incrementStartByASecond = changeTimers(start = Duration.ofSeconds(1))

    def successful(f: ScenarioContext => ScenarioContext = c => c) = recordContext { c: ScenarioContext => {
      Future.successful(f(c))
    }}

    def failing(e: Exception) = recordContext { c: ScenarioContext => {
      Future.failed(e)
    }}

    def async(f: ScenarioContext => ScenarioContext = c => c) = recordContext { c: ScenarioContext => {
      val p = Promise[ScenarioContext]()
      steps :+= { () => p.success(f(c)) }
      p.future
    }}

    private var steps = Seq[() => Any]()
    def advance() = steps match {
      case h :: t => steps = t; h()
      case _ => throw new IllegalStateException("No steps left!")
    }

    var contexts = Seq.empty[(ScenarioContext, ScenarioContext)]
    private def recordContext(action: ScenarioAction): ScenarioAction = { sc: ScenarioContext =>
      action(sc).map(ec => {
        contexts :+= (sc, ec)
        ec
      })
    }
    def actionsExecuted = contexts.length

    var metricsSent = Seq.empty[TimedActionMetrics]
    lazy val metricsRecorder = TestActorRef(new Actor {
      override def receive: Receive = {
        case m: TimedActionMetrics => metricsSent :+= m
      }
    })

    def changeTimers(start: Duration = Duration.ZERO, elapsedTime: Duration = Duration.ZERO): ScenarioContext => ScenarioContext = { c =>
      c.copy(lastAction = c.lastAction.copy(
        start = c.lastAction.start.plus(start),
        elapsedTime = elapsedTime
      ))
    }
  }

}
