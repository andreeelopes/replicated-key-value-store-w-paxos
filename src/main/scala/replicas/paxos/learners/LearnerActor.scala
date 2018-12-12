package replicas.paxos.learners

import akka.actor.{Actor, ActorLogging}
import replicas.paxos.{Init, LockedValue}
import replicas.statemachinereplication.UpdateReplicas
import utils.ReplicaNode

class LearnerActor extends Actor with ActorLogging {

  var replicas: Set[ReplicaNode] = _
  var myNode: ReplicaNode = _
  var decided: Boolean = false

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case LockedValue(value) =>
     //log.info(s"  Receive(LOCKED_VALUE, $value) from: $sender")

      if (!decided) {
       //log.info(s"  I learner $myNode have decided = $value")
        decided = true
        //myNode.smrActor ! DecisionDelivery(value)
      }
  }


}


