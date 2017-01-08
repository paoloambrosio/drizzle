package net.paoloambrosio.drizzle.gatling.http

import jodd.csselly.CSSelly
import jodd.lagarto.dom.{LagartoDOMBuilder, NodeSelector}
import net.paoloambrosio.drizzle.core.expression.Expression
import net.paoloambrosio.drizzle.gatling.core.checks.CheckBuilder
import net.paoloambrosio.drizzle.gatling.core.expression.ExpressionSupport
import net.paoloambrosio.drizzle.http.HttpResponse
import net.paoloambrosio.drizzle.http.checks.HttpCheck

package object checks {

  trait HttpChecks {

    import ExpressionSupport._

    val status = new HttpResponseStatusCheckBuilder

    def css(expression: String, attribute: String) = new HttpResponseCssCheckBuilder(expression, attribute)
  }

  trait HttpCheckBuilder[T] extends CheckBuilder[HttpResponse, T]

  class HttpResponseStatusCheckBuilder extends HttpCheckBuilder[Integer] {

    override def actual(response: HttpResponse) = response.status

    override def is(expected: Integer): HttpCheck = buildCheck { r =>
      if (r != expected) throw new Exception(s"Check failed: ${r} was not ${expected}") // TODO
    }
  }

  class HttpResponseCssCheckBuilder(expression: Expression[String], attribute: String) {

    import scala.collection.JavaConversions._

    def saveAs(varName: String): HttpCheck = (sc, httpResponse) => {
      val maybeValue = expression(sc).toOption.flatMap { cssExp =>
        val selectors = CSSelly.parse(cssExp)
        val document = new LagartoDOMBuilder().parse(httpResponse.body)
        val nodeSelector = new NodeSelector(document)
        val first = nodeSelector.select(selectors).headOption
        first.flatMap(node =>
          Option(node.getAttribute(attribute))
        )
      }
      val newContext = maybeValue.map(value =>
        sc.copy(sessionVariables = sc.sessionVariables.updated(varName, value))
      ).getOrElse(
        sc
      )
      (newContext, httpResponse)
    }
  }
}
