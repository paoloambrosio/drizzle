package net.paoloambrosio.drizzle.http.action

import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import net.paoloambrosio.drizzle.http._
import org.asynchttpclient._

import scala.concurrent.{Future, Promise}

trait AHCHttpActionFactory extends PostChecksActionFactory[BoundRequestBuilder] {
    this: TimedActionFactory =>

  protected def asyncHttpClient: AsyncHttpClient

  protected def httpRequestToImplementation(request: HttpRequest): BoundRequestBuilder = {
    val builder = request.method match {
      case HttpMethod.Get => asyncHttpClient.prepareGet(request.uri)
      case HttpMethod.Post => asyncHttpClient.preparePost(request.uri)
    }
    for ((name, value) <- request.headers) {
      builder.addHeader(name, value)
    }
    request.entity match {
      case HttpEntity.FormParams(params) =>
        for ((name, value) <- params) {
          builder.addFormParam(name, value)
        }
      case HttpEntity.Empty =>
    }
    builder
  }

  protected def executeRequest(builder: BoundRequestBuilder): Future[HttpResponse] = {
    val p = Promise[HttpResponse]()
    builder.execute(new AsyncCompletionHandler[Unit] {
      override def onCompleted(r: Response) { p.success(new NingHttpResponse(r)) }
      override def onThrowable(t: Throwable) { p.failure(t) }
    })
    p.future
  }

}

class NingHttpResponse(response: Response) extends HttpResponse {

  override def status: Integer = response.getStatusCode

}