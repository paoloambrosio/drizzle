package net.paoloambrosio.drizzle.runner

import java.time.Clock

import akka.actor._
import akka.pattern.pipe
import net.paoloambrosio.drizzle.core._

import scala.concurrent.ExecutionContext

object VUser {

  sealed trait State
  case object Idle extends State
  case object Running extends State

  sealed trait Data
  case object Uninitialized extends Data
  final case class Initialised(orchestrator: ActorRef, steps: Stream[ScenarioStep]) extends Data

  // IN
  final case class Start(steps: Stream[ScenarioStep])
  case object Stop
  private case class NextStep(context: ScenarioContext)

  // OUT
  case object Success
  final case class Failure(exception: Throwable)

  /**
    * Create Props for a VUser.
    *
    * @return a Props for creating a VUser
    */
  def props = Props(new VUser(Clock.systemUTC()))
}

class VUser(clock: Clock) extends Actor with FSM[VUser.State, VUser.Data] {

  import VUser._

  implicit private val ec: ExecutionContext = context.dispatcher

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenario), Uninitialized) =>
      val orchestrator = sender()
      self ! NextStep(initialContext)
      goto(Running) using Initialised(orchestrator, scenario)
  }

  when(Running) {
    case Event(NextStep(context), s @ Initialised(_, nextStep #:: rest)) =>
      execAction(nextStep, context)
      stay using s.copy(steps = rest)
    case Event(NextStep(context), Initialised(_, _)) =>
      stop()
    case Event(Stop, Initialised(_, _)) =>
      stop()
    case Event(Status.Failure(t), _) =>
      stop(FSM.Failure(t))
  }

  onTermination {
    case StopEvent(FSM.Normal, state, Initialised(orchestrator, _)) => orchestrator ! Success
    case StopEvent(FSM.Failure(cause: Throwable), state, Initialised(orchestrator, _)) =>  orchestrator ! Failure(cause)
  }

  initialize()

  private def initialContext = ScenarioContext(None)

  private def execAction(step: ScenarioStep, beginContext: ScenarioContext) = {
    step.action(beginContext) map (NextStep(_)) pipeTo self
  }
}
