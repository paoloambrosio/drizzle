package net.paoloambrosio.drizzle.gatling.http

import jodd.csselly.CSSelly
import jodd.lagarto.dom.{LagartoDOMBuilder, NodeSelector}
import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.gatling.core.checks.CheckBuilder
import net.paoloambrosio.drizzle.gatling.core.expression.ExpressionSupport
import net.paoloambrosio.drizzle.http.HttpResponse
import net.paoloambrosio.drizzle.http.checks.HttpCheck

import scala.util.{Failure, Success, Try}

package object checks {

  trait HttpChecks extends ExpressionSupport {

    val status = new HttpResponseStatusCheckBuilder

    def css(expression: String, attribute: String) = new HttpResponseCssCheckBuilder(expression, attribute)
  }

  trait HttpCheckBuilder[T] extends CheckBuilder[HttpResponse, T]

  class HttpResponseStatusCheckBuilder extends HttpCheckBuilder[Int] {

    override def actual(response: HttpResponse) = response.status

    // TODO Expression[Int]
    override def is(expected: Int): HttpCheck = buildCheck { r =>
      if (r != expected) throw new Exception(s"Check failed: ${r} was not ${expected}")
    }
  }

  class HttpResponseCssCheckBuilder(expression: Expression[String], attribute: String) {

    import scala.collection.JavaConversions._

    def saveAs(varName: String): HttpCheck = (sc, httpResponse) => for {
      cssSelector <- expression(sc)
      document <- Try { new LagartoDOMBuilder().parse(httpResponse.body) }
      selectors <- Try { CSSelly.parse(cssSelector) }
      first <- new NodeSelector(document).select(selectors).headOption
                  .toTry(new Exception("Node not found"))
      value <- Option(first.getAttribute(attribute))
                  .toTry(new Exception("Attribute not found"))
      newContext = sc.copy(sessionVariables = sc.sessionVariables.updated(varName, value))
    } yield (newContext, httpResponse)
  }

  implicit class OptionToTry[T](val o: Option[T]) extends AnyVal {
    def toTry(t: => Throwable): Try[T] = {
      o.map(Success(_)).getOrElse(Failure(t))
    }
  }
}
