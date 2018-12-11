package replicas.multidimensionalpaxos.learners

import akka.actor.{Actor, ActorLogging}
import replicas.multidimensionalpaxos.{DecisionDelivery, InitPaxos, LockedValue}
import replicas.statemachinereplication.{Event, UpdateReplicas}
import utils.ReplicaNode

case class LearnerInstance(var decided: Boolean = false, var i: Long)

class LearnerActor extends Actor with ActorLogging {

  var replicas: Set[ReplicaNode] = _
  var myNode: ReplicaNode = _
  var learnerInstances: Map[Long, LearnerInstance] = Map()


  override def receive = {

    case InitPaxos(_myNode_) =>
      myNode = _myNode_
    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case LockedValue(value, i) =>
      //println(s"  Receive(LOCKED_VALUE, $value, $i) from: $sender")
      learnerInstances += (i -> receiveLockedValue(learnerInstances.getOrElse(i, LearnerInstance(i = i)), value))

  }

  def receiveLockedValue(iLearner: LearnerInstance, value: List[Event]) = {
    if (!iLearner.decided) {
      iLearner.decided = true
//
      //println(s"I learner $myNode have decided = (value=$value, i=${iLearner.i})")
      myNode.smrActor ! DecisionDelivery(value, iLearner.i)
    }

    iLearner
  }

}


