package net.paoloambrosio.drizzle.feeder

import net.paoloambrosio.drizzle.core.ScenarioAction

trait FeederActionFactory {

  def feed(feeder: Feeder): ScenarioAction
}
