package net.paoloambrosio.drizzle.core

import java.time.Duration

trait LoadInjectionStepsFactory {

  def verticalRamp(n: Int): Stream[Duration] = {
    Stream.continually(Duration.ZERO).take(n)
  }
}
