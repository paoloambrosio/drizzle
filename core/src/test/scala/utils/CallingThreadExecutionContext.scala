package utils

import scala.concurrent.ExecutionContext

class CallingThreadExecutionContext extends ExecutionContext {
  override def execute(runnable: Runnable): Unit = runnable.run()
  override def reportFailure(cause: Throwable): Unit = cause.printStackTrace()
}
