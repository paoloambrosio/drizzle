package net.paoloambrosio.drizzle.http.akkahttp

import net.paoloambrosio.drizzle.http.HttpResponse

class AkkaHttpResponse(response: akka.http.scaladsl.model.HttpResponse) extends HttpResponse {

  def status = response.status.intValue()
}