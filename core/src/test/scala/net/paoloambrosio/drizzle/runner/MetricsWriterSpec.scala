package net.paoloambrosio.drizzle.runner

import java.time._

import akka.testkit.{TestActorRef, TestKit}
import net.paoloambrosio.drizzle.metrics.{MetricsRepository, TimedActionMetrics}
import net.paoloambrosio.drizzle.runner.MetricsWriter.VUserMetrics
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem

class MetricsWriterSpec extends TestKit(TestActorSystem())
    with FlatSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "pass metrics to all repositories" in new TestContext {
    val metricsCollector = actor()

    (mockMetricsRepository.store _).expects( where {
      tam: TimedActionMetrics => tam.elapsedTime == vuserMetrics.elapsedTime && tam.start == vuserMetrics.start
    }).once()

    metricsCollector ! vuserMetrics
  }

  // HELPERS

  trait TestContext {
    val mockMetricsRepository = mock[MetricsRepository]

    val vuserMetrics = VUserMetrics(OffsetDateTime.now(), Duration.ofMillis(34))

    def actor() = {
      TestActorRef(new MetricsWriter(mockMetricsRepository))
    }
  }

}
