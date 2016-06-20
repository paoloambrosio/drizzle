package net.paoloambrosio.drizzle.core

import java.time.Duration

import org.scalatest.{FlatSpec, Matchers}

class LoadInjectionStepsFactorySpec extends FlatSpec with Matchers {

  "verticalRamp" should "inject vusers all at the same time" in new LoadInjectionStepsFactory {
    verticalRamp(3) shouldBe Stream(Duration.ZERO, Duration.ZERO, Duration.ZERO)
  }

  it should "inject no vusers for negative parameter" in new LoadInjectionStepsFactory {
    verticalRamp(0) shouldBe Stream.empty[Duration]
    verticalRamp(-3) shouldBe Stream.empty[Duration]
  }

  "linearRamp" should "inject vusers linearly over time" in new LoadInjectionStepsFactory {
    linearRamp(2, Duration.ofSeconds(3)) shouldBe Stream(Duration.ofMillis(1500), Duration.ofMillis(1500))
  }

}
