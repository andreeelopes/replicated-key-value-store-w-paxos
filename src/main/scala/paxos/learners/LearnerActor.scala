package paxos.learners

import akka.actor.{Actor, ActorLogging}

class LearnerActor extends Actor with ActorLogging {

  var na = -1
  var va = ""
  var aset = Set()


  override def receive = {

    case AcceptOk(s, n, v) =>
      if (n > na) {
        na = n
        va = v
        aset = aset.empty
      }else if (n<na) return

      aset += s
      if(aset > world.size/2)
        node.statemachine ! DecisionDelivery

  }

}


