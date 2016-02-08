package net.paoloambrosio.drizzle.runner

import java.time._

import akka.testkit.{TestActorRef, TestKit}
import net.paoloambrosio.drizzle.metrics.{MetricsRepository, TimedActionMetrics}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem

class MetricsCollectorSpec extends TestKit(TestActorSystem())
    with FlatSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "pass metrics to all repositories" in new TestContext {
    val metricsCollector = actor()
    mockMetricsRepositories.foreach { m =>
      (m.store _).expects(metrics).once()
    }

    metricsCollector ! metrics
  }

  // HELPERS

  trait TestContext {
    val mockMetricsRepositories = Seq.fill(3)(mock[MetricsRepository])

    val metrics = TimedActionMetrics(null, null, null, OffsetDateTime.now(), Duration.ofMillis(34))

    def actor() = {
      TestActorRef(new MetricsCollector(mockMetricsRepositories))
    }
  }

}
