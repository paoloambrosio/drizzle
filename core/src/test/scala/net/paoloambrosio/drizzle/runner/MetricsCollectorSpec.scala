package net.paoloambrosio.drizzle.runner

import java.time._

import akka.testkit.{TestActorRef, TestKit}
import net.paoloambrosio.drizzle.metrics.{SimulationMetrics, MetricsRepository}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem

class MetricsCollectorSpec extends TestKit(TestActorSystem())
    with FlatSpecLike with Matchers with BeforeAndAfterAll with MockFactory {

  override def afterAll {
    TestKit.shutdownActorSystem(system)
  }

  it should "do stuff" in new TestContext {
    val metricsCollector = actor()
    val simulationStart = OffsetDateTime.now()
    val vuserRelStart = Duration.ofSeconds(12)
    val actionRelStart = Duration.ofMillis(34)

    // This should just write stuff to the repositories! No logic!
  }

  // HELPERS

  trait TestContext {
    val mockMetricsRepository = mock[MetricsRepository]
    val simulationMetrics = SimulationMetrics("", OffsetDateTime.now())

    def actor() = {
      TestActorRef(new MetricsCollector(Seq(mockMetricsRepository), simulationMetrics))
    }
  }

}
