package net.paoloambrosio.drizzle.http.ning

import net.paoloambrosio.drizzle.http.HttpResponse
import org.asynchttpclient.Response

class NingHttpResponse(response: Response) extends HttpResponse {

  override def status: Integer = response.getStatusCode

}