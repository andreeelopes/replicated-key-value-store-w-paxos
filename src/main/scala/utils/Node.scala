package utils

import akka.actor.ActorRef

case class Node(name: String, nodeID: String, testAppActor: ActorRef, smrActor: ActorRef, acceptorActor: ActorRef,
                learnerActor: ActorRef, proposerActor: ActorRef) {
  override def toString = name
  def getNodeID : String = nodeID


}

case class Start(node: Node)
