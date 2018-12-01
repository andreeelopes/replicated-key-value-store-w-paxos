package paxos.learners

import akka.actor.{Actor, ActorLogging, ActorRef}
import paxos.{AcceptOk, DecisionDelivery, Init}
import statemachinereplication.updateReplicas
import utils.{Node, Utils}

class LearnerActor extends Actor with ActorLogging {

  var na = -1
  var va = ""
  var aset = Set[ActorRef]()
  var replicas = Set[Node]()
  var myNode: Node = _

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
    case updateReplicas(_replicas_) =>
      replicas = _replicas_

    case AcceptOk(n, v) =>
      if (n < na) {}
      else {
        if (n > na) {
          na = n
          va = v
          aset = aset.empty
        }

        aset += sender

        if (Utils.majority(aset.size, replicas))
          log.info(s"Decided = $va")
        //myNode.smrActor ! DecisionDelivery(va)
      }
  }

}


