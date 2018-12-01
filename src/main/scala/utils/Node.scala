package utils

import akka.actor.ActorRef

case class Node(name: String, testAppActor: ActorRef, smrActor: ActorRef, acceptorActor: ActorRef,
                learnerActor: ActorRef, proposerActor: ActorRef) {
  override def toString = "name"
}

case class Start(node: Node)
