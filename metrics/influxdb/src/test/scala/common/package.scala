import java.time.{Duration => jDuration}
import java.time.temporal.{ChronoUnit => jChronoUnit}
import java.util.concurrent.{TimeUnit => jTimeUnit}

import scala.concurrent.duration.FiniteDuration
import scala.util.Random

package object common {

  private lazy val random = new Random()

  def randomAlphaNumeric(length: Int) = (random.alphanumeric take length).mkString

  implicit def scalaDurationToJava(value: FiniteDuration): jDuration = jDuration.of(value.length, value.unit)

  implicit def timeUnitToChronoUnit(value: jTimeUnit): jChronoUnit = {
    import jTimeUnit._
    value match {
      case MINUTES => jChronoUnit.MINUTES
      case SECONDS => jChronoUnit.SECONDS
      case MILLISECONDS => jChronoUnit.MILLIS
      case MICROSECONDS => jChronoUnit.MICROS
      case NANOSECONDS => jChronoUnit.NANOS
      case _ => ???
    }
  }
}
