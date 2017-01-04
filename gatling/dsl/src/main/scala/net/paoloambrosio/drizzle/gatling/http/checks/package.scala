package net.paoloambrosio.drizzle.gatling.http

import net.paoloambrosio.drizzle.gatling.core.checks.CheckBuilder
import net.paoloambrosio.drizzle.http.HttpResponse
import net.paoloambrosio.drizzle.http.checks.HttpCheck

package object checks {

  trait HttpChecks {
    val status = new HttpResponseStatusCheckBuilder
  }

  trait HttpCheckBuilder[T] extends CheckBuilder[HttpResponse, T]

  class HttpResponseStatusCheckBuilder extends HttpCheckBuilder[Integer] {

    override def actual(response: HttpResponse) = response.status

    def is(expected: Integer): HttpCheck = buildCheck { r =>
      if (r != expected) throw new Exception(s"Check failed: ${r} was not ${expected}") // TODO
    }
  }

}
