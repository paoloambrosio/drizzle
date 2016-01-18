package net.paoloambrosio.drizzle.runner

import java.time.{Clock, Duration, OffsetDateTime}

import akka.actor.{Actor, Props}
import net.paoloambrosio.drizzle.core._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object VUser {

  case object Start // IN

  private case class NextStep(context: ScenarioContext)

  /**
    * Create Props for a VUser.
    *
    * @param scenario Scenario to run
    * @return a Props for creating a VUser
    */
  def props(scenario: Scenario, clock: Clock = Clock.systemUTC()): Props = Props(new VUser(scenario, clock))

}

// TODO use FSM Running/NotRunning?
// NotRunning -> Start(scenario)
// Running -> NextStep(context), Stop
class VUser(scenario: Scenario, clock: Clock) extends Actor {

  import VUser._

  implicit private val ec: ExecutionContext = context.dispatcher

  private var steps = Stream.empty[ScenarioStep]

  override def receive = {
    case Start => {
      steps = scenario.steps
      processStep(initialContext)
    }
    case NextStep(context) => {
      // TODO check that it comes from this same actor!
      processStep(context)
    }
  }

  private def initialContext = ScenarioContext(ActionTimers(OffsetDateTime.now(clock), Duration.ZERO))

  private def processStep(beginContext: ScenarioContext) = {
    steps match {
      case head #:: tail => {
        steps = tail
        head.action(beginContext).onComplete {
          case Success(endContext) => {
            self ! NextStep(endContext)
          }
          case Failure(t) => // TODO send message to the oerchestrator and die?
        }
      }
      case _ => // TODO send message to the oerchestrator and die?
    }
  }

}
