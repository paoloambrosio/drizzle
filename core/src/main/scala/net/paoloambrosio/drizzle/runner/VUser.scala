package net.paoloambrosio.drizzle.runner

import java.time.{OffsetDateTime, Duration}

import akka.actor.{Actor, Props, Scheduler}
import net.paoloambrosio.drizzle.core._

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

object VUser {

  case object Run
  private case class NextStep(context: ScenarioContext)

  /**
    * Create Props for a VUser.
    *
    * @param scenario Scenario to run
    * @return a Props for creating a VUser
    */
  def props(scenario: Scenario): Props = Props(new VUser(scenario))

}

class VUser(scenario: Scenario) extends Actor {

  import VUser._

  implicit private val ec: ExecutionContext = context.dispatcher

  private var steps = Stream.empty[ScenarioStep]

  override def receive = {
    case Run => {
      steps = scenario.steps
      val initialContext = ScenarioContext(ActionTimers(OffsetDateTime.now(), Duration.ZERO))
      processStep(initialContext)
    }
    case NextStep(context) => {
      processStep(context)
    }
  }

  private def processStep(beginContext: ScenarioContext) = {
    steps match {
      case head #:: tail => {
        steps = tail
        head.action(beginContext).onComplete {
          case Success(endContext) => {
            self ! NextStep(endContext)
          }
          case Failure(t) => // TODO
        }
      }
      case _ => // TODO
    }
  }

}
