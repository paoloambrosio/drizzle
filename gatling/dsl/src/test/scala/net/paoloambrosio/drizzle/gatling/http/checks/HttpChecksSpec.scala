package net.paoloambrosio.drizzle.gatling.http.checks

import net.paoloambrosio.drizzle.core.ScenarioContext
import net.paoloambrosio.drizzle.http.HttpResponse
import org.scalatest.{FlatSpecLike, Matchers}

class HttpChecksSpec extends FlatSpecLike with Matchers {

  val html =
    """<html>
      |  <body>
      |    <h1>Hello World</h1>
      |    <p>
      |      <a href="/first">First</a>
      |      <a href="/second">Second</a>
      |    </p>
      |  </body>
      |</html>
    """.stripMargin

  "css check" should "do nothing if node not found" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder("p nota", "href").saveAs("x")
    val (newContext, newResponse) = extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    )
    newContext.sessionVariables shouldBe Map.empty
  }

  it should "do nothing if attribute not found" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder("p a", "nothref").saveAs("x")
    val (newContext, newResponse) = extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    )
    newContext.sessionVariables shouldBe Map.empty
  }

  it should "extract an attribute if found" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder("p a", "href").saveAs("x")
    val (newContext, newResponse) = extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    )
    newContext.sessionVariables shouldBe Map("x" -> "/first")
  }

  trait TestContext {
    def responseWithBody(s: String) = new HttpResponse {
      override def status: Integer = ???
      override val body: String = s
    }
  }
}
