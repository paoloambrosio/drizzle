package io.gatling.core

import net.paoloambrosio.drizzle.gatling.core._

/**
  * Gatling core DSL
  */
object Predef {

  type Simulation = net.paoloambrosio.drizzle.gatling.core.Simulation

  def scenario(name: String) = Scenario(name = name)

  def atOnceUsers(users: Int): InjectionStep = AtOnceInjection(users)
}
