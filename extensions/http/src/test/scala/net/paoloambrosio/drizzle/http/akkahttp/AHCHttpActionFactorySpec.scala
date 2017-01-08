package net.paoloambrosio.drizzle.http.akkahttp

import akka.testkit.TestKit
import com.github.tomakehurst.wiremock.client.WireMock._
import net.paoloambrosio.drizzle.core.ScenarioContext
import net.paoloambrosio.drizzle.core.expression.Expression.uninterpreted
import net.paoloambrosio.drizzle.http.HttpEntity.FormParams
import net.paoloambrosio.drizzle.http.HttpMethod.{Get, Post}
import net.paoloambrosio.drizzle.http.HttpRequest
import net.paoloambrosio.drizzle.http.action.AHCHttpActionFactory
import org.asynchttpclient.DefaultAsyncHttpClient
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{CallingThreadExecutionContext, TestActorSystem, WireMockSugar}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class AHCHttpActionFactorySpec extends TestKit(TestActorSystem())
  with FlatSpecLike with Matchers with BeforeAndAfterAll
  with ScalaFutures with WireMockSugar {

  override implicit def patienceConfig = PatienceConfig(timeout = 2 second)

  val testHttpClient = new DefaultAsyncHttpClient
  val testSystem = system

  override def afterAll {
    TestKit.shutdownActorSystem(system)
    testHttpClient.close()
    super.afterAll
  }

  it should "not pass content type if not specified" in new TestContext {
    val action = httpRequest(uninterpreted(
      HttpRequest(Post, url("/"))
    ), Seq.empty)

    whenReady(action(ScenarioContext())) { _ =>
      verify(postRequestedFor(urlEqualTo("/")).withoutHeader("Content-Type"))
    }
  }

  it should "send form parameters" in new TestContext {
    val action = httpRequest(uninterpreted(
      HttpRequest(
        Post, url("/"),
        Seq.empty,
        FormParams(Seq(("A","B"),("C",":)")))
      )
    ), Seq.empty)

    whenReady(action(ScenarioContext())) { _ =>
      verify(postRequestedFor(urlEqualTo("/"))
        .withHeader("Content-Type", containing("application/x-www-form-urlencoded"))
        .withRequestBody(equalTo("A=B&C=%3A%29"))
      )
    }
  }

  it should "override content type if specified" in new TestContext {
    val action = httpRequest(uninterpreted(
      HttpRequest(
        Post, url("/"),
        Seq(("content-type", "text/plain;charset=iso-8859-1")),
        FormParams(Seq(("A","B"),("C",":)")))
      )
    ), Seq.empty)

    whenReady(action(ScenarioContext())) { _ =>
      verify(postRequestedFor(urlEqualTo("/"))
        .withHeader("Content-Type", equalTo("text/plain;charset=ISO-8859-1"))
        .withRequestBody(equalTo("A=B&C=%3A%29"))
      )
    }
  }

  it should "allow overriding the user agent" in new TestContext {
    val action = httpRequest(uninterpreted(
      HttpRequest(
        Get, url("/"),
        Seq(("user-agent", "Mozilla/5.0 (Drizzle)"))
      )
    ), Seq.empty)

    whenReady(action(ScenarioContext())) { _ =>
      verify(getRequestedFor(urlEqualTo("/"))
        .withHeader("User-Agent", equalTo("Mozilla/5.0 (Drizzle)"))
      )
    }
  }

  // HELPERS

  trait TestContext extends AHCHttpActionFactory with TestDoubleTimedActionFactory {
    implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    override val asyncHttpClient = testHttpClient

    def url(path: String) = s"http://$mockServerHost:$mockServerPort$path"
  }

}
