package net.paoloambrosio.drizzle.gatling.core

import scala.concurrent.duration.FiniteDuration

sealed trait InjectionStep
case class AtOnceInjection(users: Int) extends InjectionStep
case class RampInjection(users: Int, duration: FiniteDuration) extends InjectionStep