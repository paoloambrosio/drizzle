package net.paoloambrosio.drizzle.core

import java.time.Duration

trait LoadInjectionStepsFactory {

  def verticalRamp(n: Int): Stream[Duration] = {
    Stream.continually(Duration.ZERO).take(n)
  }

  def linearRamp(n: Int, duration: Duration): Stream[Duration] = {
    Stream.continually(duration.dividedBy(n)).take(n)
  }
}
