package net.paoloambrosio.drizzle.throttler

import java.time.Duration

import org.scalatest.{FlatSpec, Matchers}

class FixedWindowThrottlerSpec extends FlatSpec with Matchers {

  it should "not throttle if the current limit is not reached" in new FixedWindowThrottler(
      Stream(1, 0, 0, 2)
  ) {
    throttle(Duration.ofSeconds(0)) shouldBe Duration.ofSeconds(0)
    throttle(Duration.ofSeconds(3)) shouldBe Duration.ofSeconds(3)
    throttle(Duration.ofSeconds(3)) shouldBe Duration.ofSeconds(3)
  }

  it should "throttle to the first available slot if the current limit is reached" in new FixedWindowThrottler(
    Stream(1, 0, 0, 2)
  ) {
    throttle(Duration.ofSeconds(0)) shouldBe Duration.ofSeconds(0)
    throttle(Duration.ofSeconds(0)) shouldBe Duration.ofSeconds(3)
    throttle(Duration.ofSeconds(0)) shouldBe Duration.ofSeconds(3)
  }

  it should "throw when it reaches the end of the pattern" in new FixedWindowThrottler(
    Stream(0, 0, 0, 0)
  ) {
    a[IllegalStateException] shouldBe thrownBy {
      throttle(Duration.ofSeconds(0))
    }
  }

}
