package net.paoloambrosio.drizzle.gatling.http.checks

import jodd.csselly.CSSellyException
import net.paoloambrosio.drizzle.core.ScenarioContext
import net.paoloambrosio.drizzle.core.expression.Expression.uninterpreted
import net.paoloambrosio.drizzle.gatling.core.expression.ExpressionSupport._
import net.paoloambrosio.drizzle.http.HttpResponse
import org.scalatest.{FlatSpecLike, Matchers, TryValues}

import scala.util.Failure

class HttpChecksSpec extends FlatSpecLike with Matchers with TryValues {

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

  "css check" should "fail if css selector cannot be parsed" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder(uninterpreted("$"), "href").saveAs("x")
    extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    ).failure.exception shouldBe a[CSSellyException]
  }

  it should "fail if body cannot be parsed" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder(uninterpreted("p a"), "href").saveAs("x")
    extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody("<not parsable")
    ).failure.exception should have message "Node not found"
  }

  it should "fail if node not found" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder(uninterpreted("p nota"), "href").saveAs("x")
    extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    ).failure.exception should have message "Node not found"
  }

  it should "fail if attribute not found" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder(uninterpreted("p a"), "nothref").saveAs("x")
    extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    ).failure.exception should have message "Attribute not found"
  }

  it should "extract an attribute if found" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder(uninterpreted("p a"), "href").saveAs("x")
    val (newContext, newResponse) = extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    ).get

    newContext.sessionVariables shouldBe Map("x" -> "/first")
  }

  it should "fail if the expression cannot be evaluated" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder(_ => Failure(new Exception("BOOM!")), "href").saveAs("x")
    extractor(
      ScenarioContext(sessionVariables = Map.empty),
      responseWithBody(html)
    ).failure.exception should have message "BOOM!"
  }

  it should "accept context expressions for css selectors" in new TestContext {
    val extractor = new HttpResponseCssCheckBuilder("p ${containsa}".el, "href").saveAs("x")
    val (newContext, newResponse) = extractor(
      ScenarioContext(sessionVariables = Map("containsa" -> "a")),
      responseWithBody(html)
    ).get

    newContext.sessionVariables shouldBe Map("containsa" -> "a", "x" -> "/first")
  }

  trait TestContext {
    def responseWithBody(s: String) = new HttpResponse {
      override def status: Int = ???
      override val body: String = s
    }
  }
}
