package net.paoloambrosio.drizzle.http.action

import java.net.URL

import net.paoloambrosio.drizzle.core._
import net.paoloambrosio.drizzle.core.action.TimedActionFactory
import org.asynchttpclient._

import scala.concurrent.Promise

trait NingHttpActionFactory extends HttpActionFactory { this: TimedActionFactory =>

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

    override def build(): ScenarioAction = timedAction { vars =>
      val p = Promise[SessionVariables]()
      boundRequestBuilder.execute(new AsyncCompletionHandler[Unit] {
        override def onCompleted(response: Response) { p.success(vars) }
        override def onThrowable(t: Throwable) { p.failure(t) }
      })
      p.future
    }
  }

}
