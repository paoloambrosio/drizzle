package net.paoloambrosio.drizzle.core

import java.time.Duration

trait LoadInjectionStepsFactory {

  def verticalRamp(n: Int): Stream[Duration] = {
    Stream.continually(Duration.ZERO).take(n)
  }

  def rampUsers(n: Int, over: java.time.Duration) = {
    Stream.continually(over.dividedBy(n)).take(n)
  }
}
