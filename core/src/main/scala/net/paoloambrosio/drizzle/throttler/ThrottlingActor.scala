package net.paoloambrosio.drizzle.throttler
import java.time.{Clock, Duration => jDuration}

import akka.actor.{Actor, Props, Scheduler}
import net.paoloambrosio.drizzle.utils.JavaTimeConversions.javaToScala

import scala.concurrent.ExecutionContext

object ThrottlingActor {

  object ThrottlingRequest
  object ThrottlingResponse

  def props(throttler: Throttler) = Props(new ThrottlingActor(throttler: Throttler, Clock.systemUTC()))
}

class ThrottlingActor(throttler: Throttler, clock: Clock) extends Actor {
  import ThrottlingActor._

  implicit def ec: ExecutionContext = context.system.dispatcher
  def scheduler: Scheduler = context.system.scheduler

  val start = jDuration.ofMillis(clock.millis())

  override def receive: Receive = {
    case ThrottlingRequest =>
      val s = sender()
      val currentOffset = jDuration.ofMillis(clock.millis()).minus(start)
      val throttledOffset = throttler.throttle(currentOffset)
      if (currentOffset == throttledOffset) {
        s ! ThrottlingResponse
      } else {
        val delay = throttledOffset.minus(currentOffset)
        scheduler.scheduleOnce(delay) {
          s ! ThrottlingResponse
        }
      }
  }

}