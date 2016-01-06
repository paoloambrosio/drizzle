package net.paoloambrosio.drizzle.core

import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import java.time.Duration

import akka.actor.Scheduler
import akka.pattern.after

import scala.concurrent.{Future, ExecutionContext}

trait SleepActionFactory {

  /**
    * Pause from the end of the previous action.
    *
    * @param duration
    */
  def thinkTime(duration: Duration)(implicit ec: ExecutionContext, scheduler: Scheduler): ScenarioAction = {
    scenarioContext: ScenarioContext => {
      after(duration, scheduler)(Future.successful(scenarioContext))
    }
  }

  /**
    * Pause from the beginning of the previous action, or no pause if the
    * execution time was longer than the pause.
    *
    * @param duration
    */
  def pacing(duration: Duration)(implicit ec: ExecutionContext, scheduler: Scheduler): ScenarioAction = {
    scenarioContext: ScenarioContext => {
      val sleepTime = duration - scenarioContext.lastAction.elapsedTime
      after(sleepTime, scheduler)(Future.successful(scenarioContext))
    }
  }
}
