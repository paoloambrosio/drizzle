package net.paoloambrosio.drizzle.gatling.core

sealed trait InjectionStep
case class AtOnceInjection(users: Int) extends InjectionStep