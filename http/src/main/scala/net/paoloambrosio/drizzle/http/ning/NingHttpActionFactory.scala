package net.paoloambrosio.drizzle.http.ning

import java.net.URL

import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.http.{HttpActionBuilder, HttpActionFactory, HttpResponse}
import org.asynchttpclient._

import scala.concurrent.{Future, Promise}

trait NingHttpActionFactory extends HttpActionFactory {

  protected def asyncHttpClient: AsyncHttpClient

  override def httpGet(url: URL): HttpActionBuilder = new NingHttpActionBuilder(asyncHttpClient.prepareGet(url.toExternalForm))
  override def httpPost(url: URL): HttpActionBuilder = new NingHttpActionBuilder(asyncHttpClient.preparePost(url.toExternalForm))


  private class NingHttpActionBuilder(private val boundRequestBuilder: BoundRequestBuilder) extends HttpActionBuilder {

    override def headers(headers: Seq[(String, String)]): HttpActionBuilder = {
      for ((name, value) <- headers) {
        boundRequestBuilder.addHeader(name, value)
      }
      this
    }

    override def entity(params: Seq[(String, String)]): HttpActionBuilder = {
      for ((name, value) <- params) {
        boundRequestBuilder.addFormParam(name, value)
      }
      this
    }

    override def apply(vars: SessionVariables): Future[(SessionVariables, HttpResponse)] = {
      val p = Promise[(SessionVariables, HttpResponse)]()
      boundRequestBuilder.execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(r: Response) { p.success((vars, new NingHttpResponse(r))) }
        override def onThrowable(t: Throwable) { p.failure(t) }
      })
      p.future
    }
  }

}
