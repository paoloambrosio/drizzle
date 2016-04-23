package net.paoloambrosio.drizzle.throttler

import net.paoloambrosio.drizzle.core.ScenarioAction

trait ThrottledActionFactory {

  def throttle(action: ScenarioAction): ScenarioAction
}
