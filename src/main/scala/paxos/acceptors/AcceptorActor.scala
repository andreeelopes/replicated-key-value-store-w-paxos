package paxos.acceptors

import akka.actor.{Actor, ActorLogging, ActorRef}
import paxos._
import statemachinereplication.updateReplicas
import utils.Node

/**
  * @param np highest prepare
  * @param na highest accept
  * @param va highest accept val
  */
case class AcceptorInstances(np: Int = -1, na: Int = -1, va: String = "-1") {
  override def toString = s"{np=$np, na=$na, va=$na}"
}

class AcceptorActor extends Actor with ActorLogging {

  var acceptorInstances = Map[Long, AcceptorInstances]()
  var replicas = Set[Node]()
  var myNode: Node = _

  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_

    case updateReplicas(_replicas_) =>
      replicas = _replicas_

    case Prepare(n, i) =>
      val iAcceptor = acceptorInstances(i)

      log.info(s"[${System.nanoTime()}]  Receive(PREPARE, $n, $i) from: $sender")

      if (n > iAcceptor.np) {
        acceptorInstances += (i -> iAcceptor.copy(np = n))
        log.info(s"[${System.nanoTime()}]  Send(PREPARE_OK, ${iAcceptor.na}, ${iAcceptor.va}, $i) to: $sender")
        sender ! PrepareOk(iAcceptor.na, iAcceptor.va, i)
      }


    case Accept(n, v, i) =>
      val iAcceptor = acceptorInstances(i)

      log.info(s"[${System.nanoTime()}]  Receive(Accept, $n, $v, $i) | State($i) = $iAcceptor")

      if (n >= iAcceptor.np) {
        acceptorInstances += (i -> iAcceptor.copy(na = n, va = v))
        sender ! AcceptOk(n, i)
        log.info(s"[${System.nanoTime()}]  Send(ACCEPT_OK , $n, $i) to: $sender")
      }

  }

}
