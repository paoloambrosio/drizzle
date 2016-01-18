package net.paoloambrosio.drizzle.core.action

import java.time.Duration

import akka.actor.Scheduler
import akka.pattern.after
import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.utils.JavaTimeConversions._

import scala.concurrent.{ExecutionContext, Future}

trait AkkaSchedulerSleepActionFactory extends SleepActionFactory {

  implicit def ec: ExecutionContext
  implicit def scheduler: Scheduler

  /**
    * Pause from the end of the previous action.
    *
    * @param duration
    */
  override def thinkTime(duration: Duration): ScenarioAction = {
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
  override def pacing(duration: Duration): ScenarioAction = {
    scenarioContext: ScenarioContext => {
      val sleepTime = duration - scenarioContext.lastAction.elapsedTime
      after(sleepTime, scheduler)(Future.successful(scenarioContext))
    }
  }
}
