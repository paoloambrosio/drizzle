package io.gatling.core

import net.paoloambrosio.drizzle.gatling.core._
import net.paoloambrosio.drizzle.gatling.core.injection.{InjectionSupport, RampInjectionBuilder}

/**
  * Gatling core DSL
  */
object Predef extends InjectionSupport {

  type Simulation = net.paoloambrosio.drizzle.gatling.core.Simulation

  def scenario(name: String) = Scenario(name = name)
}

