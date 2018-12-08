package paxos.learners

import akka.actor.{Actor, ActorLogging}
import paxos.{Init, LockedValue}
import statemachinereplication.updateReplicas
import utils.Node

class LearnerActor extends Actor with ActorLogging {

  var replicas: Set[Node] = _
  var myNode: Node = _
  var learnerInstances: Map[Long, Boolean] = Map() //[instance, decided]

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
    case updateReplicas(_replicas_) =>
      replicas = _replicas_

    case LockedValue(value, i) =>
      log.info(s"[${System.nanoTime()}]  Receive(LOCKED_VALUE, $value, $i) from: $sender")

      if (!learnerInstances(i)) {
        log.info(s"[${System.nanoTime()}]  I learner $myNode have decided = (value=$value, i=$i)")
        learnerInstances(i) = true
        //myNode.smrActor ! DecisionDelivery(value, i)
      }
  }


}


