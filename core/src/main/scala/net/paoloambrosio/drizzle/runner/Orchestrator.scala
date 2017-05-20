package net.paoloambrosio.drizzle.runner

import java.time.Clock

import akka.actor.{Actor, ActorRef, FSM, Props}
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.events.VUserEventSource
import net.paoloambrosio.drizzle.utils.CDStream

object Orchestrator {

  sealed trait State
  case object Idle extends State      // VUsers not provisioned
  case object Running extends State   // VUsers running

  sealed trait Data
  case object Uninitialized extends Data
  final case class Initialised(runner: ActorRef, vusers: Seq[ActorRef]) extends Data

  // IN
  final case class Start(scenarios: Seq[Scenario])

  // OUT
  case object Finished // TODO track if the run was successful or not returning "stats"

  def props(vUserEventSource: VUserEventSource): Props = Props(new Orchestrator(Clock.systemUTC(), VUser.props, vUserEventSource))

}

class Orchestrator(clock: Clock, vuserProps: Props, vUserEventSource: VUserEventSource)
    extends Actor with FSM[Orchestrator.State, Orchestrator.Data] {

  import Orchestrator._

  implicit val executor = context.dispatcher

  startWith(Idle, Uninitialized)

  when(Idle) {
    case Event(Start(scenarios), Uninitialized) =>
      val runner = sender()
      val vusers = scenarios.map(startNewVUser(_))
      updateState(runner, vusers)
  }

  when(Running) {
    case Event(VUser.Success | VUser.Failure(_), Initialised(runner, vusers)) =>
      vUserEventSource.fireVUserTerminated()
      val vusersLeft = vusers.filterNot(_ == sender())
      updateState(runner, vusersLeft)
  }

  initialize()

  private def updateState(runner: ActorRef, vusers: Seq[ActorRef]) = {
    if (vusers.isEmpty) {
      runner ! Finished
      stop(FSM.Normal, Uninitialized)
    } else {
      goto(Running) using Initialised(runner, vusers)
    }
  }

  private def startNewVUser(scenario: Scenario): ActorRef = {
    val vuser = context.actorOf(vuserProps)
    vUserEventSource.fireVUserCreated()
    val stepStream = toStepStream(scenario.steps)
    vuser ! VUser.Start(stepStream)
    vuser
  }

  // TODO move somewhere else (like a factory trait)
  def toStepStream(steps: Seq[ScenarioStep]): StepStream = {
    steps.map {
      case s: ActionStep => CDStream.static((c: ScenarioContext) => ActionExecutor(s.name.map(_.apply(c).get), () => s.action(c)))
      case s: LoopStep => CDStream.loop(s.condition)(toStepStream(s.body))
      case s: ConditionalStep => CDStream.conditional(s.condition)(toStepStream(s.body))
    }.foldLeft(CDStream.empty[ScenarioContext, ActionExecutor])((h,t) => h.append(t))
  }

}
