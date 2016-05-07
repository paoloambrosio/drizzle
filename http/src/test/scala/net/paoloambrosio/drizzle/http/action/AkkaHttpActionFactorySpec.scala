package net.paoloambrosio.drizzle.http.action

import java.net.URL

import akka.stream.ActorMaterializer
import akka.testkit.TestKit
import com.github.tomakehurst.wiremock.client.WireMock._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.core.{ScenarioAction, ScenarioContext, SessionVariables}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{BeforeAndAfterAll, FlatSpecLike, Matchers}
import utils.{CallingThreadExecutionContext, TestActorSystem, WireMockSugar}

import scala.concurrent.{ExecutionContext, Future}

class AkkaHttpActionFactorySpec extends TestKit(TestActorSystem())
  with FlatSpecLike with Matchers with BeforeAndAfterAll
  with ScalaFutures with WireMockSugar {

  val testSystem = system

  override def afterAll {
    super.afterAll
    TestKit.shutdownActorSystem(system)
  }

  it should "not pass content type if not specified" in new TestContext {
    val action: ScenarioAction = httpPost(url("/")).build()

    whenReady(action(ScenarioContext())) { _ =>
      verify(postRequestedFor(urlEqualTo("/")).withoutHeader("Content-Type"))
    }
  }

  it should "send form parameters" in new TestContext {
    val action: ScenarioAction = httpPost(url("/")).entity(Seq(("A","B"),("C",":)"))).build()

    whenReady(action(ScenarioContext())) { _ =>
      verify(postRequestedFor(urlEqualTo("/"))
        .withHeader("Content-Type", equalTo("application/x-www-form-urlencoded; charset=UTF-8"))
        .withRequestBody(equalTo("A=B&C=%3A%29"))
      )
    }
  }

  // HELPERS

  trait TestContext extends AkkaHttpActionFactory with TimedActionFactory {
    override implicit val system = testSystem
    override implicit def materializer = ActorMaterializer()
    override implicit val ec: ExecutionContext = new CallingThreadExecutionContext
    override def timedAction(f: (SessionVariables) => Future[SessionVariables]): ScenarioAction = {
      sc: ScenarioContext => f(sc.sessionVariables).map(vars => sc.copy(sessionVariables = vars))
    }

    def url(path: String): URL = new URL(s"http://$mockServerHost:$mockServerPort$path")
  }

}
