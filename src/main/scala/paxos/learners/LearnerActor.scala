package paxos.learners

import akka.actor.{Actor, ActorLogging}
import paxos.{Init, LockedValue}
import statemachinereplication.UpdateReplicas
import utils.Node

class LearnerActor extends Actor with ActorLogging {

  var replicas: Set[Node] = _
  var myNode: Node = _
  var decided: Boolean = false

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case LockedValue(value) =>
      log.info(s"[${System.nanoTime()}]  Receive(LOCKED_VALUE, $value) from: $sender")

      if (!decided) {
        log.info(s"[${System.nanoTime()}]  I learner $myNode have decided = $value")
        decided = true
        //myNode.smrActor ! DecisionDelivery(value)
      }
  }


}


