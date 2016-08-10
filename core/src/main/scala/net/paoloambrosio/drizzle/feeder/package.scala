package net.paoloambrosio.drizzle

import net.paoloambrosio.drizzle.core.SessionVariables

package object feeder {

  type Feeder = Iterator[SessionVariables]
}
