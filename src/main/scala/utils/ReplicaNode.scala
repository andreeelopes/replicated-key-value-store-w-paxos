package utils

import akka.actor.ActorRef

case class ReplicaNode(name: String, nodeID: String, smrActor: ActorRef, acceptorActor: ActorRef,
                       learnerActor: ActorRef, proposerActor: ActorRef) {
  override def toString = name

  def getNodeID: String = nodeID


}

case class Start(node: ReplicaNode)
