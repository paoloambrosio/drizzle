package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.feeder.Feeder

import scala.concurrent.duration.Duration

trait Action
case class PauseAction(duration: Duration) extends Action
case class FeedingAction(feeder: Feeder) extends Action
