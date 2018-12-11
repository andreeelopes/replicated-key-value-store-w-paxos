package replicas.multidimensionalpaxos.acceptors

import akka.actor.{Actor, ActorLogging}
import replicas.multidimensionalpaxos._
import replicas.statemachinereplication.{Event, UpdateReplicas}
import utils.ReplicaNode

/**
  * @param np highest prepare
  * @param na highest accept
  * @param va highest accept val
  */
case class AcceptorInstance(var np: Int = -1, var na: Int = -1, var va: List[Event] = null, var i: Long) {
  override def toString = s"{np=$np, na=$na, va=$na}"
}

class AcceptorActor extends Actor with ActorLogging {

  var acceptorInstances = Map[Long, AcceptorInstance]()
  var replicas = Set[ReplicaNode]()
  var myNode: ReplicaNode = _


  override def receive = {

    case InitPaxos(_myNode_) =>
      myNode = _myNode_

    case UpdateReplicas(_replicas_) =>
      replicas = _replicas_

    case Prepare(n, i) =>
      //println(s"  Receive(PREPARE, $n, $i) from: $sender")
      acceptorInstances += (i -> receivePrepare(acceptorInstances.getOrElse(i, AcceptorInstance(i = i)), n))

    case Accept(n, v, i) =>
      //println(s"  Receive(Accept, $n, $v, $i) | State($i) = ${acceptorInstances(i)}")
      acceptorInstances += (i -> receiveAccept(acceptorInstances.getOrElse(i, AcceptorInstance(i = i)), n, v))

  }

  def receivePrepare(iAcceptor: AcceptorInstance, n: Int): AcceptorInstance = {
    if (n > iAcceptor.np) {
      iAcceptor.np = n

      //println(s"  Send(PREPARE_OK, ${iAcceptor.na}, ${iAcceptor.va}, ${iAcceptor.i}) to: $sender")
      sender ! PrepareOk(iAcceptor.na, iAcceptor.va, iAcceptor.i, n)
    }

    iAcceptor
  }

  def receiveAccept(iAcceptor: AcceptorInstance, n: Int, v: List[Event]) = {
    if (n >= iAcceptor.np) {
      iAcceptor.na = n
      iAcceptor.va = v

      //println(s"  Send(ACCEPT_OK , $n, ${iAcceptor.i}) to: $sender")
      sender ! AcceptOk(iAcceptor.na, iAcceptor.i, n)
    }

    iAcceptor
  }


}
