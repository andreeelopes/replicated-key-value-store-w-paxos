package paxos.acceptors

import akka.actor.{Actor, ActorLogging}

class AcceptorActor extends Actor with ActorLogging {

  var np = -1
  var na = -1
  var va = ""
  var learners = List()

  override def receive = {

    case Prepare(s, n) =>
      if (n > np) {
        np = n
        sender ! PrepareOk(s, na, va)
      } else
        PrepareNotOk(s)

    case Accept(s, n, v) =>

      if (n >= np) {
        na = n
        va = v
        learners.foreach(l => l ! AcceptOk(l, na, va))
      } else {
        sender ! AcceptNotOk(s)
      }

  }

}
