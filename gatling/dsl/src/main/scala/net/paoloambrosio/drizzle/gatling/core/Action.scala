package net.paoloambrosio.drizzle.gatling.core

import scala.concurrent.duration.Duration

trait Action
case class PauseAction(duration: Duration) extends Action
