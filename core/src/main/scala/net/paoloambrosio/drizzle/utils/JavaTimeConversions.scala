package net.paoloambrosio.drizzle.utils

import java.{time => j}

import scala.concurrent.{duration => s}

object JavaTimeConversions {

  implicit def scalaToJava(value: s.Duration): j.Duration = {
    j.Duration.ofNanos(value.toNanos)
  }

  implicit def javaToScala(value: j.Duration): s.FiniteDuration = {
    s.Duration.fromNanos(value.toNanos)
  }
}
