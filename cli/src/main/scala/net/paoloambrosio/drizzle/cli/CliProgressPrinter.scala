package net.paoloambrosio.drizzle.cli

import akka.actor.{Actor, Props}

object CliProgressPrinter {

  def props() = Props(new CliProgressPrinter)
}

class CliProgressPrinter() extends Actor {

  import net.paoloambrosio.drizzle.runner.events._

  var createdVusers = 0
  var runningVusers = 0

  override def receive: Receive = {
    case VUserCreated =>
      createdVusers += 1
      println(s"${Console.GREEN}[${runningVusers}/${createdVusers}]${Console.RESET}")
    case VUserStarted =>
      runningVusers += 1
      println(s"${Console.YELLOW}[${runningVusers}/${createdVusers}]${Console.YELLOW}")
    case VUserTerminated =>
      runningVusers -= 1
      println(s"${Console.RED}[${runningVusers}/${createdVusers}]${Console.RESET}")
    case VUserMetrics(start, elapsedTime) =>
      println(s"${Console.BLUE}[${start} ${elapsedTime}]${Console.RESET}")
  }
}
