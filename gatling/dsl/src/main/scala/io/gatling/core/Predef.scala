package io.gatling.core

import net.paoloambrosio.drizzle.gatling.core._
import net.paoloambrosio.drizzle.gatling.core.feeders.StandardFeeders
import net.paoloambrosio.drizzle.gatling.core.injection.InjectionSupport

/**
  * Gatling core DSL
  */
object Predef extends InjectionSupport with StandardFeeders {

  type Simulation = net.paoloambrosio.drizzle.gatling.core.Simulation

  def scenario(name: String) = Scenario(name)

}
