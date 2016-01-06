package net.paoloambrosio.drizzle.core

import java.time.Duration

import org.scalatest.{FlatSpec, Matchers}

class LoadInjectionStepsFactorySpec extends FlatSpec with Matchers {

  "verticalRamp" should "inject vusers all at the same time" in {
    LoadInjectionStepsFactory.verticalRamp(3) shouldBe Stream(Duration.ZERO, Duration.ZERO, Duration.ZERO)
  }

  it should "inject no vusers for negative parameter" in {
    LoadInjectionStepsFactory.verticalRamp(0) shouldBe Stream.empty[Duration]
    LoadInjectionStepsFactory.verticalRamp(-3) shouldBe Stream.empty[Duration]
  }

}
