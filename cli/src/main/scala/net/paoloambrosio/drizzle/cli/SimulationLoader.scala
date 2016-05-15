package net.paoloambrosio.drizzle.cli

import net.paoloambrosio.drizzle.core.Simulation

trait SimulationLoader {

  def load(ref: String): Simulation

}