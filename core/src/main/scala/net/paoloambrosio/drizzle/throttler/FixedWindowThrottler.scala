package net.paoloambrosio.drizzle.throttler
import java.time.Duration

import scala.annotation.tailrec

/**
  * Simple throttler that uses a fixed window to track and throttle requests.
  * It offers weak guarantees similar to [[akka.contrib.throttle.TimerBasedThrottler]].
  *
  * @param pattern throttling pattern in request per second
  */
class FixedWindowThrottler(pattern: Stream[Int]) extends Throttler {

  val patternIterator = pattern.iterator
  var available = 0
  var currentOffset = -1

  override def throttle(offset: Duration): Duration = {
    val offsetInSeconds = (offset.toMillis / 1000).toInt
    var offsetFromCurrent = if (offsetInSeconds > currentOffset) offsetInSeconds - currentOffset else 0
    if (offsetFromCurrent > 0)
      move(offsetFromCurrent)
    takeFirstAvailableSpace()
    Duration.ofSeconds(currentOffset - offsetInSeconds).plus(offset)
  }

  private def move(i: Int) = {
    currentOffset += i
    patternIterator.drop(i-1)
    if (patternIterator.isEmpty)
      throw new IllegalStateException("Reached the end of the throttling pattern")
    available = patternIterator.next()
  }

  private def takeFirstAvailableSpace() = {
    @tailrec
    def takeFirstAvailableSpaceAcc(i: Int): Unit = {
      available = available - 1
      if (available < 0) {
        move(1)
        takeFirstAvailableSpaceAcc(i+1)
      }
    }

    takeFirstAvailableSpaceAcc(0)
  }

}
