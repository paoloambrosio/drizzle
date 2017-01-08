package net.paoloambrosio.drizzle.gatling.http

import jodd.csselly.CSSelly
import jodd.lagarto.dom.{LagartoDOMBuilder, NodeSelector}
import net.paoloambrosio.drizzle.gatling.core.checks.CheckBuilder
import net.paoloambrosio.drizzle.http.HttpResponse
import net.paoloambrosio.drizzle.http.checks.HttpCheck

package object checks {

  trait HttpChecks {
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

  class HttpResponseCssCheckBuilder(expression: String, attribute: String) {

    import scala.collection.JavaConversions._

    val selectors = CSSelly.parse(expression)

    def saveAs(varName: String): HttpCheck = (sc, httpResponse) => {
      val document = new LagartoDOMBuilder().parse(httpResponse.body)
      val nodeSelector = new NodeSelector(document)
      val first = nodeSelector.select(selectors).headOption
      val newContext = first.flatMap( node =>
        Option(node.getAttribute(attribute))
      ).map( value =>
        sc.copy(sessionVariables = sc.sessionVariables.updated(varName, value))
      ).getOrElse(
        sc
      )
      (newContext, httpResponse)
    }
  }
}
