package multidimensionalpaxos.learners

import akka.actor.{Actor, ActorLogging}
import multidimensionalpaxos.{Init, LockedValue}
import statemachinereplication.{Event, Operation, updateReplicas}
import utils.Node

case class LearnerInstance(var decided: Boolean = false, var i: Long)

class LearnerActor extends Actor with ActorLogging {

  var replicas: Set[Node] = _
  var myNode: Node = _
  var learnerInstances: Map[Long, LearnerInstance] = Map()


  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_
    case updateReplicas(_replicas_) =>
      replicas = _replicas_

    case LockedValue(value, i) =>
      log.info(s"[${System.nanoTime()}]  Receive(LOCKED_VALUE, $value, $i) from: $sender")
      learnerInstances += (i -> receiveLockedValue(learnerInstances.getOrElse(i, LearnerInstance(i = i)), value))

  }

  def receiveLockedValue(iLearner: LearnerInstance, value: Event) = {
    if (!iLearner.decided) {
      iLearner.decided = true

      log.info(s"[${System.nanoTime()}]  I learner $myNode have decided = (value=$value, i=${iLearner.i})")
      //myNode.smrActor ! DecisionDelivery(value, iLearner.i)
    }

    iLearner
  }

}


