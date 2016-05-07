package net.paoloambrosio.drizzle.runner

import java.time._

import akka.testkit.{TestActorRef, TestKit}
import net.paoloambrosio.drizzle.metrics.{MetricsRepository, TimedActionMetrics}
import net.paoloambrosio.drizzle.runner.events.{MetricsWriter, VUserMetrics}
import org.mockito.ArgumentCaptor
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.TestActorSystem
import org.mockito.Mockito._
import org.scalatest.mock.MockitoSugar

class MetricsWriterSpec extends TestKit(TestActorSystem())
    with FlatSpecLike with Matchers with BeforeAndAfterAll with MockitoSugar {

  override def afterAll {
    super.afterAll
    TestKit.shutdownActorSystem(system)
  }

  it should "pass metrics to all repositories" in new TestContext {
    val argument = ArgumentCaptor.forClass(classOf[TimedActionMetrics])
    val metricsCollector = actor()

    metricsCollector ! vuserMetrics

    verify(mockMetricsRepository, times(1)).store(argument.capture())

    val tam = argument.getValue
    tam.elapsedTime shouldBe vuserMetrics.elapsedTime
    tam.start shouldBe vuserMetrics.start
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
