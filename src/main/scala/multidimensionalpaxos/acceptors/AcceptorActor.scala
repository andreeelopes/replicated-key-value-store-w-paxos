package multidimensionalpaxos.acceptors

import akka.actor.{Actor, ActorLogging}
import multidimensionalpaxos._
import statemachinereplication.{Event, UpdateReplicas}
import utils.Node

/**
  * @param np highest prepare
  * @param na highest accept
  * @param va highest accept val
  */
case class AcceptorInstance(var np: Int = -1, var na: Int = -1, var va: Event = null, var i: Long) {
  override def toString = s"{np=$np, na=$na, va=$na}"
}

class AcceptorActor extends Actor with ActorLogging {

  var acceptorInstances = Map[Long, AcceptorInstance]()
  var replicas = Set[Node]()
  var myNode: Node = _


  override def receive = {

    case Init(_replicas_, _myNode_) =>
      replicas = _replicas_
      myNode = _myNode_

    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case Prepare(n, i) =>
      //log.info(s"[${System.nanoTime()}]  Receive(PREPARE, $n, $i) from: $sender")
      acceptorInstances += (i -> receivePrepare(acceptorInstances.getOrElse(i, AcceptorInstance(i = i)), n))

    case Accept(n, v, i) =>
      //log.info(s"[${System.nanoTime()}]  Receive(Accept, $n, $v, $i) | State($i) = ${acceptorInstances(i)}")
      acceptorInstances += (i -> receiveAccept(acceptorInstances.getOrElse(i, AcceptorInstance(i = i)), n, v))

  }

  def receivePrepare(iAcceptor: AcceptorInstance, n: Int): AcceptorInstance = {
    if (n > iAcceptor.np) {
      iAcceptor.np = n

      //log.info(s"[${System.nanoTime()}]  Send(PREPARE_OK, ${iAcceptor.na}, ${iAcceptor.va}, ${iAcceptor.i}) to: $sender")
      sender ! PrepareOk(iAcceptor.na, iAcceptor.va, iAcceptor.i)
    }

    iAcceptor
  }

  def receiveAccept(iAcceptor: AcceptorInstance, n: Int, v: Event) = {
    if (n >= iAcceptor.np) {
      iAcceptor.na = n
      iAcceptor.va = v

      //log.info(s"[${System.nanoTime()}]  Send(ACCEPT_OK , $n, ${iAcceptor.i}) to: $sender")
      sender ! AcceptOk(iAcceptor.na, iAcceptor.i)
    }

    iAcceptor
  }


}
