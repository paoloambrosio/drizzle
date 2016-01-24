package net.paoloambrosio.drizzle.core

import java.time.Duration

import net.paoloambrosio.drizzle.core.action.SleepActionFactory

trait ScenarioStreamFactory { this: SleepActionFactory =>

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
      sp.scenario.copy(steps = initialDelayStep(startDelay) +: sp.scenario.steps)
    })
  }

  private def startDelays(loadProfile: Seq[Duration]): Seq[Duration] = {
    loadProfile.scanLeft(Duration.ZERO)((acc, v) => acc.plus(v)).tail
  }

  private def initialDelayStep(startDelay: Duration) = {
    ScenarioStep("", thinkTime(startDelay))
  }

}
