package net.paoloambrosio.drizzle

import net.paoloambrosio.drizzle.core.action.TimedActionFactory._

package object checks {

  type Check[T] = NotTimedPart[T]

}
