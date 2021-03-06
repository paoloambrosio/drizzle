package net.paoloambrosio.drizzle.core

import java.time.Duration

import net.paoloambrosio.drizzle.core.action.SleepActionFactory
import net.paoloambrosio.drizzle.core.events.VUserEventSource

import scala.concurrent.ExecutionContext

trait ScenarioStreamFactory { this: SleepActionFactory =>

  def vUserEventSource: VUserEventSource
  implicit def ec: ExecutionContext

  /**
    * Converts a sequence of scenario profiles in a sequence of single
    * scenarios where the start is delayed according to the load profile.
    *
    * @param scenarioProfiles sequence of scenario profiles
    * @return sequenece of scenarios
    */
  def scenarioStream(scenarioProfiles: Seq[ScenarioProfile]): Seq[Scenario] = {
    scenarioProfiles.flatMap(scenarioStream(_))
  }

  private def scenarioStream(sp: ScenarioProfile): Seq[Scenario] = {
    startDelays(sp.loadProfile).map(startDelay => {
      sp.scenario.copy(steps = initialDelayStep(startDelay) +: wrapSendingMetrics(sp.scenario.steps))
    })
  }

  private def startDelays(loadProfile: Seq[Duration]): Seq[Duration] = {
    loadProfile.scanLeft(Duration.ZERO)((acc, v) => acc.plus(v)).tail
  }

  private def initialDelayStep(startDelay: Duration) = {
    ActionStep(None, initialDelayAction(startDelay))
  }

  private def initialDelayAction(startDelay: Duration) = thinkTime(startDelay) andThen { out =>
    out.onComplete(v => vUserEventSource.fireVUserStarted())
    out
  }

  private def wrapSendingMetrics(steps: Seq[ScenarioStep]): Seq[ScenarioStep] = steps.map {
    case s: ActionStep => s.copy(action = wrapActionSendingMetrics(s.action))
    case s: LoopStep => s.copy(body = wrapSendingMetrics(s.body))
    case s: ConditionalStep => s.copy(body = wrapSendingMetrics(s.body))
  }

  private def wrapActionSendingMetrics(action: ScenarioAction) = action andThen { out =>
    out.onSuccess {
      case ScenarioContext(ActionResult(Some(at), None), _) => vUserEventSource.fireVUserMetrics(at)
    }
    out
  }

}
