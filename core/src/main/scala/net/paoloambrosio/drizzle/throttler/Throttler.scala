package net.paoloambrosio.drizzle.throttler

import java.time.Duration

trait Throttler {

  /**
    * Returns when the action can be scheduled according to the
    * implemented throttling algorithm.
    *
    * This should not be considered thread safe.
    *
    * @param originalOffset original offset of the request from
    *                       the beginning of the scenario
    * @return throttled offset when to schedule the action
    *         (same as the original for no throttling)
    */
  def throttle(originalOffset: Duration): Duration
}
