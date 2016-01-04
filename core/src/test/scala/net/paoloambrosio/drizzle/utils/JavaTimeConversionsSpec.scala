package net.paoloambrosio.drizzle.utils

import java.{time => j}

import scala.concurrent.{duration => s}
import org.scalatest._
import JavaTimeConversions._

import scala.util.Random

class JavaTimeConversionsSpec extends FlatSpec with Matchers {

  val n = new Random().nextInt(100)

  "javaToScala" should "convert all durations" in {
    javaToScala(j.Duration.ofMinutes(n)) should be (s.Duration(n, s.MINUTES))
    javaToScala(j.Duration.ofSeconds(n)) should be (s.Duration(n, s.SECONDS))
    javaToScala(j.Duration.ofMillis(n)) should be (s.Duration(n, s.MILLISECONDS))
    javaToScala(j.Duration.ofNanos(n * 1000)) should be (s.Duration(n, s.MICROSECONDS))
    javaToScala(j.Duration.ofNanos(n)) should be (s.Duration(n, s.NANOSECONDS))
  }

  "scalaToJava" should "convert all finite durations" in {
    scalaToJava(s.Duration(n, s.MINUTES)) should be (j.Duration.ofMinutes(n))
    scalaToJava(s.Duration(n, s.SECONDS)) should be (j.Duration.ofSeconds(n))
    scalaToJava(s.Duration(n, s.MILLISECONDS)) should be (j.Duration.ofMillis(n))
    scalaToJava(s.Duration(n, s.MICROSECONDS)) should be (j.Duration.ofNanos(n * 1000))
    scalaToJava(s.Duration(n, s.NANOSECONDS)) should be (j.Duration.ofNanos(n))
  }

  it should "error on nonfinite durations" in {
    intercept[IllegalArgumentException] { scalaToJava(s.Duration.Inf) }
    intercept[IllegalArgumentException] { scalaToJava(s.Duration.MinusInf) }
    intercept[IllegalArgumentException] { scalaToJava(s.Duration.Undefined) }
  }
}
