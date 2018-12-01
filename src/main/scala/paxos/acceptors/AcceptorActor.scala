package paxos.acceptors

import akka.actor.{Actor, ActorLogging, ActorRef}
import paxos._
import statemachinereplication.updateReplicas
import utils.Node

class AcceptorActor extends Actor with ActorLogging {

  var np = -1
  var na = -1
  var va = ""

  var replicas = Set[Node]()
  var myNode: Node = _

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_

    case updateReplicas(_replicas_) =>
      replicas = _replicas_

    case Prepare(n) =>
      if (n > np) {
        np = n
        sender ! PrepareOk(na, va)
      } else
        PrepareNotOk

    case Accept(n, v) =>

      if (n >= np) {
        na = n
        va = v
        replicas.foreach(r => r.learnerActor ! AcceptOk(na, va))
      } else {
        sender ! AcceptNotOk
      }

  }

}
