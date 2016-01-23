package net.paoloambrosio.drizzle.runner

import java.time.{Clock, Duration, OffsetDateTime}

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
  final case class Initialised(runner: ActorRef, scenario: Scenario) extends Data

  // IN
  final case class Start(scenario: Scenario)
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
  def props(clock: Clock = Clock.systemUTC()): Props = Props(new VUser(clock))

}

class VUser(clock: Clock) extends Actor with FSM[VUser.State, VUser.Data] {

  import VUser._

  implicit private val ec: ExecutionContext = context.dispatcher

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenario: Scenario), Uninitialized) =>
      val runner = sender()
      self ! NextStep(initialContext)
      goto(Running) using Initialised(runner, scenario)
  }

  when(Running) {
    case Event(NextStep(context), s @ Initialised(_, Scenario(_, nextStep #:: rest))) =>
      execAction(nextStep, context)
      stay using s.copy(scenario = s.scenario.copy(steps = rest))
    case Event(NextStep(context), Initialised(runner, _)) =>
      stop()
    case Event(Stop, Initialised(runner, _)) =>
      stop()
    case Event(Status.Failure(t), _) =>
      stop(FSM.Failure(t))
  }

  onTermination {
    case StopEvent(FSM.Normal, state, Initialised(runner, _)) => runner ! Success
    case StopEvent(FSM.Failure(cause: Throwable), state, Initialised(runner, _)) =>  runner ! Failure(cause)
  }

  initialize()

  private def initialContext = ScenarioContext(ActionTimers(OffsetDateTime.now(clock), Duration.ZERO))

  private def execAction(step: ScenarioStep, beginContext: ScenarioContext) = {
    step.action(beginContext) map(NextStep(_)) pipeTo self
  }

}
