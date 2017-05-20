package net.paoloambrosio.drizzle.runner

import java.time.Clock

import akka.actor._
import akka.pattern.pipe
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.StepStream.@::

import scala.concurrent.ExecutionContext

object VUser {

  sealed trait State
  case object Idle extends State
  case object Running extends State

  sealed trait Data
  case object Uninitialized extends Data
  final case class Initialised(orchestrator: ActorRef, steps: StepStream) extends Data

  // IN
  final case class Start(steps: StepStream)
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
    case Event(NextStep(sc), s @ Initialised(_, steps)) => {
      implicit val isc = sc
      steps match {
        case actionExecutor @:: rest =>
          exec(actionExecutor)
          stay using s.copy(steps = rest)
        case _ =>
          stop()
      }
    }
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

  private def exec(actionExecutor: ActionExecutor): Unit = {
    actionExecutor.action() map (NextStep(_)) pipeTo self
  }
}
