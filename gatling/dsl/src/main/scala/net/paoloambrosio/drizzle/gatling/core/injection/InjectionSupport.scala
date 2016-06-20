package net.paoloambrosio.drizzle.gatling.core.injection

import net.paoloambrosio.drizzle.gatling.core.{AtOnceInjection, RampInjection}

import scala.concurrent.duration.FiniteDuration

trait InjectionSupport {

  def atOnceUsers(users: Int) = AtOnceInjection(users)
  def rampUsers(users: Int) = RampInjectionBuilder(users)
}

case class RampInjectionBuilder(users: Int) {
  def over(duration: FiniteDuration) = RampInjection(users, duration)
}
