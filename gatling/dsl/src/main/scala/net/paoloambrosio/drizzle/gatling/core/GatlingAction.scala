package net.paoloambrosio.drizzle.gatling.core

import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.feeder.Feeder

import scala.concurrent.duration.Duration

trait GatlingAction
case class PauseAction(duration: Duration) extends GatlingAction
case class FeedingAction(feeder: Feeder) extends GatlingAction
case class LoopAction(times: Expression[Int], counterName: String, body: List[GatlingAction]) extends GatlingAction
