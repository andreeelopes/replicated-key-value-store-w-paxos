package replicas.multidimensionalpaxos.learners

import akka.actor.{Actor, ActorLogging}
import replicas.multidimensionalpaxos.{DecisionDelivery, Init, LockedValue}
import replicas.statemachinereplication.{Event, UpdateReplicas}
import utils.ReplicaNode

case class LearnerInstance(var decided: Boolean = false, var i: Long)

class LearnerActor extends Actor with ActorLogging {

  var replicas: Set[ReplicaNode] = _
  var myNode: ReplicaNode = _
  var learnerInstances: Map[Long, LearnerInstance] = Map()


  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case LockedValue(value, i) =>
      //log.info(s"[${System.nanoTime()}]  Receive(LOCKED_VALUE, $value, $i) from: $sender")
      learnerInstances += (i -> receiveLockedValue(learnerInstances.getOrElse(i, LearnerInstance(i = i)), value))

  }

  def receiveLockedValue(iLearner: LearnerInstance, value: Event) = {
    if (!iLearner.decided) {
      iLearner.decided = true

      //log.info(s"[${System.nanoTime()}]  I learner $myNode have decided = (value=$value, i=${iLearner.i})")
      myNode.smrActor ! DecisionDelivery(value, iLearner.i)
    }

    iLearner
  }

}


